
package org.telescope.protocol;

import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.BitUtil;
import org.telescope.javel.framework.helper.Parser;
import org.telescope.javel.framework.helper.PatternBuilder;
import org.telescope.javel.framework.helper.UnitsConverter;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.NetworkMessage;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class MobilogixProtocolDecoder extends BaseProtocolDecoder {

    public MobilogixProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("[")
            .number("(dddd)-(dd)-(dd) ")         // date (yyyymmdd)
            .number("(dd):(dd):(dd),")           // time (hhmmss)
            .number("Td,")                       // type
            .number("d+,")                       // device type
            .expression("[^,]+,")                // protocol version
            .expression("([^,]+),")              // serial number
            .number("(xx),")                     // status
            .number("(d+.d+),")                  // battery
            .number("(d)")                       // valid
            .number("(d)")                       // rssi
            .number("(d),")                      // satellites
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+.d+),")                // longitude
            .number("(d+.?d*),")                 // speed
            .number("(d+.?d*)")                  // course
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;
        String type = sentence.substring(21, 21 + 2);

        if (channel != null) {
            String time = sentence.substring(1, 20);
            String response;
            if (type.equals("T1")) {
                response = String.format("[%s,S1,1]", time);
            } else {
                response = String.format("[%s,S%c]", time, type.charAt(1));
            }
            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
        }

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());

        position.setTime(parser.nextDateTime());

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        int status = parser.nextHexInt();
        position.set(Position.KEY_IGNITION, BitUtil.check(status, 2));
        position.set(Position.KEY_MOTION, BitUtil.check(status, 3));
        position.set(Position.KEY_STATUS, status);

        position.set(Position.KEY_BATTERY, parser.nextDouble());

        position.setValid(parser.nextInt() > 0);

        position.set(Position.KEY_RSSI, parser.nextInt());
        position.set(Position.KEY_SATELLITES, parser.nextInt());

        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));
        position.setCourse(parser.nextDouble());

        return position;
    }

}
