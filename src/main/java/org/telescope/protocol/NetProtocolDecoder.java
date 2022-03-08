
package org.telescope.protocol;

import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.BitUtil;
import org.telescope.javel.framework.helper.Parser;
import org.telescope.javel.framework.helper.PatternBuilder;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class NetProtocolDecoder extends BaseProtocolDecoder {

    public NetProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("@L")
            .number("ddd")
            .number("(d{15})")                   // imei
            .number("xx")
            .number("(dd)(dd)(dd)")              // date (ddmmyy)
            .number("(dd)(dd)(dd)")              // time (hhmmss)
            .number("(x)")                       // flags
            .number("(dd)(dd)(dddd)")            // latitude
            .number("(ddd)(dd)(dddd)")           // longitude
            .number("(x{8})")                    // status
            .number("(x{4})")                    // speed
            .number("(x{6})")                    // odometer
            .number("(xxx)")                     // course
            .number("(xxx)")                     // alarm
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

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

        int flags = parser.nextHexInt();

        position.setValid(BitUtil.check(flags, 3));
        int hemisphereLatitude = BitUtil.check(flags, 1) ? -1 : 1;
        int hemisphereLongitude = BitUtil.check(flags, 0) ? -1 : 1;

        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN) * hemisphereLatitude);
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN) * hemisphereLongitude);

        position.set(Position.KEY_STATUS, parser.nextHexLong());
        position.setSpeed(parser.nextHexInt() * 0.01);
        position.set(Position.KEY_ODOMETER, parser.nextHexInt() * 1852.0 / 16);
        position.setCourse(parser.nextHexInt());

        parser.nextHexInt(); // alarm

        return position;
    }

}
