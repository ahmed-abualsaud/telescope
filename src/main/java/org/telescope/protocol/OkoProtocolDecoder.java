
package org.telescope.protocol;

import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.DateBuilder;
import org.telescope.javel.framework.helper.Parser;
import org.telescope.javel.framework.helper.PatternBuilder;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class OkoProtocolDecoder extends BaseProtocolDecoder {

    public OkoProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("{")
            .number("(d{15}),").optional()       // imei
            .number("(dd)(dd)(dd)(?:.d+)?,")     // time
            .expression("([AV]),")               // validity
            .number("(dd)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(ddd)(dd.d+),")             // longitude
            .expression("([EW]),")
            .number("(d+.?d*)?,")                // speed
            .number("(d+.?d*)?,")                // course
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .number("(d+),")                     // satellites
            .number("(d+.d+|xx),")               // adc
            .number("(xx),")                     // event
            .number("(d+.d+|xx),")               // power
            .number("d")                         // memory status
            .number(",(xx)").optional()          // io
            .any()
            .compile();

    private double decodeVoltage(Parser parser) {
        String value = parser.next();
        if (value.contains(".")) {
            return Double.parseDouble(value);
        } else {
            return Integer.parseInt(value, 16) * 0.1;
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession;
        if (parser.hasNext()) {
            deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        } else {
            deviceSession = getDeviceSession(channel, remoteAddress);
        }
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        DateBuilder dateBuilder = new DateBuilder()
                .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt());

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        dateBuilder.setDateReverse(parser.nextInt(), parser.nextInt(), parser.nextInt());
        position.setTime(dateBuilder.getDate());

        position.set(Position.KEY_SATELLITES, parser.nextInt());
        position.set(Position.PREFIX_ADC + 1, decodeVoltage(parser));
        position.set(Position.KEY_EVENT, parser.next());
        position.set(Position.KEY_POWER, decodeVoltage(parser));
        position.set(Position.KEY_INPUT, parser.nextHexInt());

        return position;
    }

}
