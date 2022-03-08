
package org.telescope.protocol;

import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.DateBuilder;
import org.telescope.javel.framework.helper.Parser;
import org.telescope.javel.framework.helper.PatternBuilder;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.NetworkMessage;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class RaceDynamicsProtocolDecoder extends BaseProtocolDecoder {

    public RaceDynamicsProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    public static final int MSG_LOGIN = 12;
    public static final int MSG_LOCATION = 15;

    private static final Pattern PATTERN_LOGIN = new PatternBuilder()
            .text("$GPRMC,")
            .number("d+,")                       // type
            .number("d{6},")                     // date
            .number("d{6},")                     // time
            .number("(d{15}),")
            .compile();

    private static final Pattern PATTERN_LOCATION = new PatternBuilder()
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .expression("([AV]),")               // validity
            .number("(dd)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(ddd)(dd.d+),")             // longitude
            .expression("([EW]),")
            .number("(d+),")                     // speed
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .number("(-?d+),")                   // altitude
            .number("(d+),")                     // satellites
            .number("([01]),")                   // ignition
            .number("(d+),")                     // index
            .text("%,")
            .number("([^,]+),")                  // ibutton
            .number("d+,")                       // acceleration
            .number("d+,")                       // deceleration
            .number("[01],")                     // cruise control
            .number("[01],")                     // seat belt
            .number("[01],")                     // wrong ibutton
            .number("(d+),")                     // power
            .number("[01],")                     // power status
            .number("(d+),")                     // battery
            .number("([01]),")                   // panic
            .number("d+,")
            .number("d+,")
            .number("(d),")                      // overspeed
            .number("d+,")                       // speed limit
            .number("d+,")                       // tachometer
            .number("d+,d+,d+,")                 // aux
            .number("d+,")                       // geofence id
            .number("d+,")                       // road speed type
            .number("d+,")                       // ibutton count
            .number("(d),")                      // overdriver alert
            .any()
            .compile();

    private String imei;

    private void sendResponse(Channel channel, SocketAddress remoteAddress, int type) {
        if (channel != null) {
            String response = String.format(
                    "$GPRMC,%1$d,%2$td%2$tm%2$ty,%2$tH%2$tM%2$tS,%3$s,\r\n", type, new Date(), imei);
            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        int type = Integer.parseInt(sentence.substring(7, 9));

        if (type == MSG_LOGIN) {

            Parser parser = new Parser(PATTERN_LOGIN, sentence);
            if (parser.matches()) {
                imei = parser.next();
                getDeviceSession(channel, remoteAddress, imei);
                sendResponse(channel, remoteAddress, type);
            }

        } else if (type == MSG_LOCATION) {

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
            if (deviceSession == null) {
                return null;
            }

            List<Position> positions = new LinkedList<>();

            for (String data : sentence.substring(17, sentence.length() - 3).split(",#,#,")) {
                Parser parser = new Parser(PATTERN_LOCATION, data);
                if (parser.matches()) {

                    Position position = new Position(getProtocolName());
                    position.setDeviceId(deviceSession.getDeviceId());

                    DateBuilder dateBuilder = new DateBuilder()
                            .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt());

                    position.setValid(parser.next().equals("A"));
                    position.setLatitude(parser.nextCoordinate());
                    position.setLongitude(parser.nextCoordinate());
                    position.setSpeed(parser.nextDouble());

                    dateBuilder.setDateReverse(parser.nextInt(), parser.nextInt(), parser.nextInt());
                    position.setTime(dateBuilder.getDate());

                    position.setAltitude(parser.nextInt());
                    position.set(Position.KEY_SATELLITES, parser.nextInt());
                    position.set(Position.KEY_IGNITION, parser.nextInt() == 1);
                    position.set(Position.KEY_INDEX, parser.nextInt());
                    position.set(Position.KEY_DRIVER_UNIQUE_ID, parser.next());
                    position.set(Position.KEY_POWER, parser.nextInt() * 0.01);
                    position.set(Position.KEY_BATTERY, parser.nextInt() * 0.01);
                    position.set(Position.KEY_ALARM, parser.nextInt() > 0 ? Position.ALARM_SOS : null);
                    position.set(Position.KEY_ALARM, parser.nextInt() > 0 ? Position.ALARM_OVERSPEED : null);

                    int overDriver = parser.nextInt();
                    if (overDriver > 0) {
                        position.set("overDriver", overDriver);
                    }

                    positions.add(position);

                }
            }

            sendResponse(channel, remoteAddress, type);

            return positions;

        }

        return null;
    }

}