
package org.telescope.protocol;

import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.Parser;
import org.telescope.javel.framework.helper.PatternBuilder;
import org.telescope.javel.framework.helper.UnitsConverter;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class Xt013ProtocolDecoder extends BaseProtocolDecoder {

    public Xt013ProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .number("HI,d+").optional()
            .text("TK,")
            .number("(d+),")                     // imei
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("([+-]d+.d+),")              // latitude
            .number("([+-]d+.d+),")              // longitude
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("d+,")
            .number("(d+),")                     // altitude
            .expression("([FL]),")               // gps fix
            .number("d+,")
            .number("(d+),")                     // gps level
            .number("x+,")
            .number("x+,")
            .number("(d+),")                     // gsm level
            .expression("[^,]*,")
            .number("(d+.d+),")                  // battery
            .number("(d),")                      // charging
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(parser.nextDateTime());

        position.setLatitude(parser.nextDouble(0));
        position.setLongitude(parser.nextDouble(0));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));
        position.setCourse(parser.nextDouble(0));
        position.setAltitude(parser.nextDouble(0));
        position.setValid(parser.next().equals("F"));

        position.set(Position.KEY_SATELLITES, parser.nextInt());
        position.set(Position.KEY_RSSI, parser.nextDouble());
        position.set(Position.KEY_BATTERY, parser.nextDouble(0));
        position.set(Position.KEY_CHARGE, parser.next().equals("1"));

        return position;
    }

}
