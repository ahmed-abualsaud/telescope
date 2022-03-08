
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

public class SmartSoleProtocolDecoder extends BaseProtocolDecoder {

    public SmartSoleProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("#GTXRP=")
            .number("(d+),")                     // imei
            .number("d+,")                       // report type
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(-?d+.d+),")                // longitude
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+),")                   // altitude
            .number("(d+),")                     // speed
            .number("([01]),")                   // valid
            .number("(d+),")                     // satellites
            .number("(d+.d+),")                  // hdop
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(d+.d+),")                  // battery
            .number("(d+)")                      // status
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

        position.setFixTime(parser.nextDateTime());

        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());
        position.setAltitude(parser.nextInt());
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt()));
        position.setValid(parser.nextInt() == 1);

        position.set(Position.KEY_SATELLITES, parser.nextInt());
        position.set(Position.KEY_HDOP, parser.nextDouble());

        position.setDeviceTime(parser.nextDateTime());

        position.set(Position.KEY_BATTERY, parser.nextDouble());
        position.set(Position.KEY_STATUS, parser.nextInt());

        return position;
    }

}
