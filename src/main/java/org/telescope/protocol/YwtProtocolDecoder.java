
package org.telescope.protocol;

import io.netty.channel.Channel;

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

public class YwtProtocolDecoder extends BaseProtocolDecoder {

    public YwtProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .expression("%(..),")                // type
            .number("(d+):")                     // unit identifier
            .number("d+,")                       // subtype
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .expression("([EW])")
            .number("(ddd.d{6}),")               // longitude
            .expression("([NS])")
            .number("(dd.d{6}),")                // latitude
            .number("(d+)?,")                    // altitude
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("(d+),")                     // satellite
            .expression("([^,]+),")              // report identifier
            .expression("([-0-9a-fA-F]+)")       // status
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        // Synchronization
        if (sentence.startsWith("%SN") && channel != null) {
            int start = sentence.indexOf(':');
            int end = start;
            for (int i = 0; i < 4; i++) {
                end = sentence.indexOf(',', end + 1);
            }
            if (end == -1) {
                end = sentence.length();
            }

            channel.writeAndFlush(new NetworkMessage("%AT+SN=" + sentence.substring(start, end), remoteAddress));
            return null;
        }

        Parser parser = new Parser(PATTERN, sentence);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());

        String type = parser.next();

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(parser.nextDateTime());

        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
        position.setAltitude(parser.nextDouble(0));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));
        position.setCourse(parser.nextDouble());

        int satellites = parser.nextInt();
        position.setValid(satellites != 0);
        position.set(Position.KEY_SATELLITES, satellites);

        String reportId = parser.next();

        position.set(Position.KEY_STATUS, parser.next());

        // Send response
        if ((type.equals("KP") || type.equals("EP")) && channel != null) {
            channel.writeAndFlush(new NetworkMessage("%AT+" + type + "=" + reportId + "\r\n", remoteAddress));
        }

        return position;
    }

}
