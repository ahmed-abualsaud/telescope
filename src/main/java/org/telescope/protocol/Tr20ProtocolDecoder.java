
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

public class Tr20ProtocolDecoder extends BaseProtocolDecoder {

    public Tr20ProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN_PING = new PatternBuilder()
            .text("%%")
            .expression("[^,]+,")
            .number("(d+)")
            .compile();

    private static final Pattern PATTERN_DATA = new PatternBuilder()
            .text("%%")
            .expression("([^,]+),")              // id
            .expression("([AL]),")               // validity
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .expression("([NS])")
            .number("(dd)(dd.d+)")               // latitude
            .expression("([EW])")
            .number("(ddd)(dd.d+),")             // longitude
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("(?:NA|[FC]?(-?d+)[^,]*),")  // temperature
            .number("(x{8}),")                   // status
            .number("(d+)")                      // event
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN_PING, (String) msg);
        if (parser.matches()) {
            if (channel != null) {
                channel.writeAndFlush(new NetworkMessage(
                        "&&" + parser.next() + "\r\n", remoteAddress)); // keep-alive response
            }
            return null;
        }

        parser = new Parser(PATTERN_DATA, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        position.setValid(parser.next().equals("A"));

        position.setTime(parser.nextDateTime());

        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));
        position.setCourse(parser.nextDouble());

        position.set(Position.PREFIX_TEMP + 1, parser.nextInt());
        position.set(Position.KEY_STATUS, parser.nextHexLong());
        position.set(Position.KEY_EVENT, parser.nextInt());

        return position;
    }

}
