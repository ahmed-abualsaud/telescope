
package org.telescope.protocol;

import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.Parser;
import org.telescope.javel.framework.helper.PatternBuilder;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.NetworkMessage;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.util.Date;
import java.util.regex.Pattern;

public class GpsmtaProtocolDecoder extends BaseProtocolDecoder {

    public GpsmtaProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .expression("([^ ]+) ")              // uid
            .number("(d+) ")                     // time (unix time)
            .number("(-?d+.d+) ")                // latitude
            .number("(-?d+.d+) ")                // longitude
            .number("(d+) ")                     // speed
            .number("(d+) ")                     // course
            .number("(d+) ")                     // accuracy
            .number("(d+) ")                     // altitude
            .number("(d+) ")                     // flags
            .number("(d+) ")                     // battery
            .number("(d+) ")                     // temperature
            .number("(d)")                       // charging status
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

        String time = parser.next();
        position.setTime(new Date(Long.parseLong(time) * 1000));

        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());
        position.setSpeed(parser.nextInt());
        position.setCourse(parser.nextInt());
        position.setAccuracy(parser.nextInt());
        position.setAltitude(parser.nextInt());

        position.set(Position.KEY_STATUS, parser.nextInt());
        position.set(Position.KEY_BATTERY_LEVEL, parser.nextInt());
        position.set(Position.PREFIX_TEMP + 1, parser.nextInt());
        position.set(Position.KEY_CHARGE, parser.nextInt() == 1);

        if (channel != null) {
            channel.writeAndFlush(new NetworkMessage(time, remoteAddress));
        }

        return position;
    }

}
