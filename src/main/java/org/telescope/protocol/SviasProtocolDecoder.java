
package org.telescope.protocol;

import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.regex.Pattern;

import org.telescope.javel.framework.helper.BitUtil;
import org.telescope.javel.framework.helper.Parser;
import org.telescope.javel.framework.helper.PatternBuilder;
import org.telescope.javel.framework.helper.UnitsConverter;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.NetworkMessage;
import org.telescope.server.Protocol;

public class SviasProtocolDecoder extends BaseProtocolDecoder {

    public SviasProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("[")                           // delimiter
            .number("d{4},")                     // hardware version
            .number("d{4},")                     // software version
            .number("d+,")                       // index
            .number("(d+),")                     // imei
            .number("d+,")                       // hour meter
            .number("(d+)(dd)(dd),")             // date (dmmyy)
            .number("(d+)(dd)(dd),")             // time (hmmss)
            .number("(-?)(d+)(dd)(d{5}),")       // latitude
            .number("(-?)(d+)(dd)(d{5}),")       // longitude
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("(d+),")                     // odometer
            .number("(d+),")                     // input
            .number("(d+),")                     // output / status
            .number("(d),")
            .number("(d),")
            .number("(d+),")                     // power
            .number("(d+),")                     // battery level
            .number("(d+),")                     // rssi
            .any()
            .compile();

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg)
            throws Exception {

        if (channel != null) {
            channel.writeAndFlush(new NetworkMessage("@", remoteAddress));
        }

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_MIN));
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_MIN));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble() * 0.01));
        position.setCourse(parser.nextDouble() * 0.01);

        position.set(Position.KEY_ODOMETER, parser.nextInt() * 100);

        int input = parser.nextInt();
        int output = parser.nextInt();

        position.set(Position.KEY_ALARM, BitUtil.check(input, 0) ? Position.ALARM_SOS : null);
        position.set(Position.KEY_IGNITION, BitUtil.check(input, 4));
        position.setValid(BitUtil.check(output, 0));

        position.set(Position.KEY_POWER, parser.nextInt() * 0.001);
        position.set(Position.KEY_BATTERY_LEVEL, parser.nextInt());
        position.set(Position.KEY_RSSI, parser.nextInt());

        return position;
    }

}
