
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

public class C2stekProtocolDecoder extends BaseProtocolDecoder {

    public C2stekProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("PA$")
            .number("(d+)")                      // imei
            .text("$")
            .expression(".#")                    // data type
            .number("(dd)(dd)(dd)#")             // date (yymmdd)
            .number("(dd)(dd)(dd)#")             // time (hhmmss)
            .number("([01])#")                   // valid
            .number("([+-]?d+.d+)#")             // latitude
            .number("([+-]?d+.d+)#")             // longitude
            .number("(d+.d+)#")                  // speed
            .number("(d+.d+)#")                  // course
            .number("(-?d+.d+)#")                // altitude
            .number("(d+)#")                     // battery
            .number("d+#")                       // geo area alarm
            .number("(x+)#")                     // alarm
            .number("([01])")                    // armed
            .number("([01])")                    // door
            .number("([01])#")                   // ignition
            .any()
            .text("$AP")
            .compile();

    private String decodeAlarm(int alarm) {
        switch (alarm) {
            case 0x2:
                return Position.ALARM_SHOCK;
            case 0x3:
                return Position.ALARM_POWER_CUT;
            case 0x4:
                return Position.ALARM_OVERSPEED;
            case 0x5:
                return Position.ALARM_SOS;
            case 0x6:
                return Position.ALARM_DOOR;
            case 0xA:
                return Position.ALARM_LOW_BATTERY;
            case 0xB:
                return Position.ALARM_FAULT;
            default:
                return null;
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;
        if (sentence.contains("$20$") && channel != null) {
            channel.writeAndFlush(new NetworkMessage(sentence, remoteAddress));
        }

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

        position.setTime(parser.nextDateTime());
        position.setValid(parser.nextInt() > 0);
        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));
        position.setCourse(parser.nextDouble());
        position.setAltitude(parser.nextDouble());

        position.set(Position.KEY_BATTERY, parser.nextInt() * 0.001);
        position.set(Position.KEY_ALARM, decodeAlarm(parser.nextHexInt()));

        position.set(Position.KEY_ARMED, parser.nextInt() > 0);
        position.set(Position.KEY_DOOR, parser.nextInt() > 0);
        position.set(Position.KEY_IGNITION, parser.nextInt() > 0);

        return position;
    }

}
