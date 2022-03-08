
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.BitUtil;
import org.telescope.javel.framework.helper.DateBuilder;
import org.telescope.javel.framework.helper.Parser;
import org.telescope.javel.framework.helper.PatternBuilder;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class TekProtocolDecoder extends BaseProtocolDecoder {

    public TekProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .number(",d+,")
            .number("(dd)(dd)(dd).d,")           // time (hhmmss)
            .number("(dd)(dd.d+)")               // latitude
            .expression("([NS]),")
            .number("(ddd)(dd.d+)")              // longitude
            .expression("([EW]),")
            .number("(d+.d+),")                  // hdop
            .number("(d+.d+),")                  // altitude
            .number("(d+),")                     // fix mode
            .number("(d+.d+),")                  // course
            .number("d+.d+,")                    // speed km
            .number("(d+.d+),")                  // speed kn
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .number("(d+),")                     // satellites
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.readUnsignedByte(); // product type
        buf.readUnsignedByte(); // hardware version
        buf.readUnsignedByte(); // firmware version
        buf.readUnsignedByte(); // contact reason
        buf.readUnsignedByte(); // alarm / status
        buf.readUnsignedByte(); // rssi
        buf.readUnsignedByte(); // battery / status

        String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(1);
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
        if (deviceSession == null) {
            return null;
        }

        int type = BitUtil.to(buf.readUnsignedByte(), 6);
        buf.readUnsignedByte(); // length

        if (type == 4 || type == 8) {

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            int count = buf.readUnsignedShort();
            buf.readUnsignedByte(); // hours / tickets
            buf.readUnsignedByte(); // error code
            buf.readUnsignedByte(); // reserved
            buf.readUnsignedByte(); // logger speed
            buf.readUnsignedByte(); // login time
            buf.readUnsignedByte(); // minutes

            for (int i = 0; i < count; i++) {
                position.set("rssi" + (i + 1), buf.readUnsignedByte());
                position.set("temp" + (i + 1), buf.readUnsignedByte() - 30);
                int data = buf.readUnsignedShort();
                position.set("src" + (i + 1), BitUtil.from(data, 10));
                position.set("ullage" + (i + 1), BitUtil.to(data, 10));
            }

            return position;

        } else if (type == 17) {

            String sentence = buf.toString(StandardCharsets.US_ASCII);

            Parser parser = new Parser(PATTERN, sentence);
            if (!parser.matches()) {
                return null;
            }

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            DateBuilder dateBuilder = new DateBuilder()
                    .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt());

            position.setLatitude(parser.nextCoordinate());
            position.setLongitude(parser.nextCoordinate());

            position.set(Position.KEY_HDOP, parser.nextDouble());

            position.setAltitude(parser.nextDouble());
            position.setValid(parser.nextInt() > 0);
            position.setCourse(parser.nextDouble());
            position.setSpeed(parser.nextDouble());

            dateBuilder.setDateReverse(parser.nextInt(), parser.nextInt(), parser.nextInt());
            position.setTime(dateBuilder.getDate());

            return position;

        }

        return null;
    }

}
