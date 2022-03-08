
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.DateBuilder;
import org.telescope.javel.framework.helper.UnitsConverter;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class WliProtocolDecoder extends BaseProtocolDecoder {

    public WliProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.readUnsignedByte(); // header
        int type = buf.readUnsignedByte();

        if (type == '1') {

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
            if (deviceSession == null) {
                return null;
            }

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            position.set(Position.KEY_INDEX, buf.readUnsignedShort());

            buf.readUnsignedShort(); // length
            buf.readUnsignedShort(); // checksum
            buf.readUnsignedByte(); // application message type
            buf.readUnsignedByte(); // delimiter

            while (buf.readableBytes() > 1) {

                int fieldNumber = buf.readUnsignedByte();

                buf.readUnsignedByte(); // delimiter

                if (buf.getUnsignedByte(buf.readerIndex()) == 0xFF) {

                    buf.readUnsignedByte(); // binary type indication
                    int endIndex = buf.readUnsignedShort() + buf.readerIndex();

                    if (fieldNumber == 52) {
                        position.setValid(true);
                        buf.readUnsignedByte(); // reason
                        buf.readUnsignedByte(); // century
                        DateBuilder dateBuilder = new DateBuilder()
                                .setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
                                .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
                        position.setFixTime(dateBuilder.getDate());
                        position.setLatitude(buf.readInt() / 600000.0);
                        position.setLongitude(buf.readInt() / 600000.0);
                        position.setSpeed(buf.readUnsignedShort());
                        position.setCourse(buf.readUnsignedShort() * 0.1);
                        position.set(Position.KEY_ODOMETER, UnitsConverter.metersFromFeet(buf.readUnsignedInt()));
                        position.setAltitude(buf.readInt() * 0.1);
                    }

                    buf.readerIndex(endIndex);

                } else {

                    int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) 0);
                    String value = buf.readCharSequence(
                            endIndex - buf.readerIndex(), StandardCharsets.US_ASCII).toString();

                    switch (fieldNumber) {
                        case 246:
                            String[] values = value.split(",");
                            position.set(Position.KEY_POWER, Integer.parseInt(values[2]) * 0.01);
                            position.set(Position.KEY_BATTERY, Integer.parseInt(values[3]) * 0.01);
                            break;
                        case 255:
                            position.setDeviceTime(new Date(Long.parseLong(value) * 1000));
                            break;
                        default:
                            break;
                    }

                }

                buf.readUnsignedByte(); // delimiter

            }

            if (!position.getValid()) {
                getLastLocation(position, position.getDeviceTime());
            }

            return position;

        } else if (type == '2') {

            String id = buf.toString(buf.readerIndex(), buf.readableBytes() - 1, StandardCharsets.US_ASCII);
            getDeviceSession(channel, remoteAddress, id.substring("wli:".length()));
            return null;

        }

        return null;
    }

}
