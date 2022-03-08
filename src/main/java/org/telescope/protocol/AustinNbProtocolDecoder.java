
package org.telescope.protocol;

import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.Parser;
import org.telescope.javel.framework.helper.PatternBuilder;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class AustinNbProtocolDecoder extends BaseProtocolDecoder {

    public AustinNbProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .number("(d+);")                     // imei
            .number("(dddd)-(dd)-(dd) ")         // date
            .number("(dd):(dd):(dd);")           // time
            .number("(-?d+,d+);")                // latitude
            .number("(-?d+,d+);")                // longitude
            .number("(d+);")                     // azimuth
            .number("(d+);")                     // angle
            .number("(d+);")                     // range
            .number("(d+);")                     // out of range
            .expression("(.*)")                  // operator
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

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.YMD_HMS, TimeZone.getDefault().getID()));

        position.setValid(true);
        position.setLatitude(Double.parseDouble(parser.next().replace(',', '.')));
        position.setLongitude(Double.parseDouble(parser.next().replace(',', '.')));
        position.setCourse(parser.nextInt());
        position.set("angle", parser.nextInt());
        position.set("range", parser.nextInt());
        position.set("outOfRange", parser.nextInt());
        position.set("carrier", parser.next());

        return position;
    }

}
