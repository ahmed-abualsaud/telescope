
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.BcdUtil;
import org.telescope.javel.framework.helper.BitUtil;
import org.telescope.javel.framework.helper.DateBuilder;
import org.telescope.javel.framework.helper.UnitsConverter;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.NetworkMessage;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class Gs100ProtocolDecoder extends BaseProtocolDecoder {

    public Gs100ProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        String header = buf.readCharSequence(2, StandardCharsets.US_ASCII).toString();

        if (header.equals("GL")) {

            buf.skipBytes(1);
            String imei = buf.readCharSequence(buf.readUnsignedByte(), StandardCharsets.US_ASCII).toString();
            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);

            if (channel != null && deviceSession != null) {
                ByteBuf response = Unpooled.copiedBuffer("GS100", StandardCharsets.US_ASCII);
                channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
            }

            return null;

        } else {

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
            if (deviceSession == null) {
                return null;
            }

            List<Position> positions = new LinkedList<>();

            int count = buf.readUnsignedByte();
            for (int i = 0; i < count; i++) {

                int endIndex = buf.readUnsignedByte() + buf.readerIndex();

                Position position = new Position(getProtocolName());
                position.setDeviceId(deviceSession.getDeviceId());

                int status = buf.readUnsignedMedium();
                position.set(Position.KEY_STATUS, status);

                if (BitUtil.check(status, 8 + 8 + 7)) {

                    DateBuilder dateBuilder = new DateBuilder()
                            .setHour(BcdUtil.readInteger(buf, 2))
                            .setMinute(BcdUtil.readInteger(buf, 2))
                            .setSecond(BcdUtil.readInteger(buf, 2))
                            .setDay(BcdUtil.readInteger(buf, 2))
                            .setMonth(BcdUtil.readInteger(buf, 2))
                            .setYear(BcdUtil.readInteger(buf, 2));
                    position.setTime(dateBuilder.getDate());

                    position.setValid(true);

                    String coordinates = ByteBufUtil.hexDump(buf.readSlice(9));
                    position.setLongitude(Integer.parseInt(coordinates.substring(0, 3))
                            + Integer.parseInt(coordinates.substring(3, 9)) * 0.0001 / 60);
                    position.setLatitude(Integer.parseInt(coordinates.substring(10, 12))
                            + Integer.parseInt(coordinates.substring(12, 18)) * 0.0001 / 60);
                    int flags = Integer.parseInt(coordinates.substring(9, 10), 16);
                    if (!BitUtil.check(flags, 3)) {
                        position.setLongitude(-position.getLongitude());
                    }
                    if (!BitUtil.check(flags, 2)) {
                        position.setLatitude(-position.getLatitude());
                    }

                    String other = ByteBufUtil.hexDump(buf.readSlice(4));
                    position.setSpeed(UnitsConverter.knotsFromKph(Integer.parseInt(other.substring(0, 5)) * 0.01));
                    position.setCourse(Integer.parseInt(other.substring(5, 8)));

                } else {

                    getLastLocation(position, null);

                }

                positions.add(position);

                buf.readerIndex(endIndex);

            }

            if (channel != null) {
                ByteBuf response = Unpooled.buffer();
                response.writeCharSequence("GS100", StandardCharsets.US_ASCII);
                response.writeByte(count);
                channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
            }

            return positions.isEmpty() ? null : positions;

        }
    }

}
