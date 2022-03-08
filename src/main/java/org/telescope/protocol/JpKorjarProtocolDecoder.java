
package org.telescope.protocol;

import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.Parser;
import org.telescope.javel.framework.helper.PatternBuilder;
import org.telescope.model.CellTower;
import org.telescope.model.Network;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class JpKorjarProtocolDecoder extends BaseProtocolDecoder {

    public JpKorjarProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("KORJAR.PL,")
            .number("(d+),")                     // imei
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(d+.d+)([NS]),")            // latitude
            .number("(d+.d+)([EW]),")            // longitude
            .number("(d+.d+),")                  // speed
            .number("(d+),")                     // course
            .number("[FL]:(d+.d+)V,")            // battery
            .number("([01]) ")                   // valid
            .number("(d+) ")                     // mcc
            .number("(d+) ")                     // mnc
            .number("(x+) ")                     // lac
            .number("(x+),")                     // cid
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

        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        position.set(Position.KEY_BATTERY, parser.nextDouble(0));

        position.setValid(parser.nextInt(0) == 1);

        position.setNetwork(new Network(CellTower.from(
                parser.nextInt(0), parser.nextInt(0), parser.nextHexInt(0), parser.nextHexInt(0))));

        return position;
    }

}
