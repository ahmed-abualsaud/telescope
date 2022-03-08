
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.BitUtil;
import org.telescope.javel.framework.helper.Checksum;
import org.telescope.javel.framework.helper.UnitsConverter;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.NetworkMessage;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class SatsolProtocolDecoder extends BaseProtocolDecoder {

    public SatsolProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.readUnsignedShortLE(); // checksum
        buf.readUnsignedShortLE(); // preamble
        long id = buf.readUnsignedIntLE();
        buf.readUnsignedShortLE(); // length

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, String.valueOf(id));
        if (deviceSession == null) {
            return null;
        }

        List<Position> positions = new LinkedList<>();

        while (buf.isReadable()) {

            buf.readUnsignedShortLE(); // checksum
            buf.readUnsignedShortLE(); // checksum
            buf.readUnsignedShortLE(); // type
            int length = buf.readUnsignedShortLE();

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            position.setTime(new Date(buf.readUnsignedIntLE() * 1000));
            position.setLatitude(buf.readUnsignedIntLE() * 0.000001);
            position.setLongitude(buf.readUnsignedIntLE() * 0.000001);
            position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShortLE() * 0.01));
            position.setAltitude(buf.readShortLE());
            position.setCourse(buf.readUnsignedShortLE());
            position.setValid(buf.readUnsignedByte() > 0);

            position.set(Position.KEY_SATELLITES, buf.readUnsignedByte());
            position.set(Position.KEY_EVENT, buf.readUnsignedByte());

            if (BitUtil.check(buf.readUnsignedByte(), 0)) {
                position.set(Position.KEY_ARCHIVE, true);
            }

            positions.add(position);

            buf.skipBytes(length);

        }

        if (channel != null) {
            ByteBuf response = Unpooled.buffer();
            response.writeShortLE(0);
            response.writeShortLE(0x4CBF); // preamble
            response.writeIntLE((int) id);
            response.writeShortLE(0);
            response.setShortLE(0, Checksum.crc16(
                    Checksum.CRC16_CCITT_FALSE, response.nioBuffer(2, response.readableBytes() - 2)));
            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
        }

        return positions;
    }

}
