
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.BitUtil;
import org.telescope.javel.framework.helper.DateBuilder;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.NetworkMessage;
import org.telescope.server.Protocol;

import java.net.SocketAddress;

public class Pt215ProtocolDecoder extends BaseProtocolDecoder {

    public Pt215ProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    public static final int MSG_LOGIN = 0x01;
    public static final int MSG_HEARTBEAT = 0x08;
    public static final int MSG_GPS_REALTIME = 0x10;
    public static final int MSG_GPS_OFFLINE = 0x11;
    public static final int MSG_STATUS = 0x13;

    private void sendResponse(
            Channel channel, SocketAddress remoteAddress, int type, ByteBuf content) {
        if (channel != null) {
            ByteBuf response = Unpooled.buffer();
            response.writeByte('X');
            response.writeByte('X');
            response.writeByte(content != null ? 1 + content.readableBytes() : 1);
            response.writeByte(type);
            if (content != null) {
                response.writeBytes(content);
                content.release();
            }
            response.writeByte('\r');
            response.writeByte('\n');
            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.skipBytes(2); // header
        buf.readUnsignedByte(); // length
        int type = buf.readUnsignedByte();

        if (type == MSG_LOGIN) {

            getDeviceSession(channel, remoteAddress, ByteBufUtil.hexDump(buf.readSlice(8)).substring(1));
            sendResponse(channel, remoteAddress, type, null);

        } else if (type == MSG_GPS_OFFLINE || type == MSG_GPS_REALTIME) {

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
            if (deviceSession == null) {
                return null;
            }

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            sendResponse(channel, remoteAddress, type, buf.retainedSlice(buf.readerIndex(), 6));

            DateBuilder dateBuilder = new DateBuilder()
                    .setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
                    .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
            position.setTime(dateBuilder.getDate());

            double latitude = buf.readUnsignedInt() / 60.0 / 30000.0;
            double longitude = buf.readUnsignedInt() / 60.0 / 30000.0;

            int flags = buf.readUnsignedShort();
            position.setCourse(BitUtil.to(flags, 10));
            position.setValid(BitUtil.check(flags, 12));

            if (!BitUtil.check(flags, 10)) {
                latitude = -latitude;
            }
            if (BitUtil.check(flags, 11)) {
                longitude = -longitude;
            }

            position.setLatitude(latitude);
            position.setLongitude(longitude);

            return position;

        }

        return null;
    }

}
