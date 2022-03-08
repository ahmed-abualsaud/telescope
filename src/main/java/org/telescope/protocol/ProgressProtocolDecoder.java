
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.BitUtil;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.NetworkMessage;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ProgressProtocolDecoder extends BaseProtocolDecoder {

    private long lastIndex;
    private long newIndex;

    public ProgressProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    public static final int MSG_NULL = 0;
    public static final int MSG_IDENT = 1;
    public static final int MSG_IDENT_FULL = 2;
    public static final int MSG_POINT = 10;
    public static final int MSG_LOG_SYNC = 100;
    public static final int MSG_LOGMSG = 101;
    public static final int MSG_TEXT = 102;
    public static final int MSG_ALARM = 200;
    public static final int MSG_ALARM_RECIEVED = 201;

    private void requestArchive(Channel channel) {
        if (lastIndex == 0) {
            lastIndex = newIndex;
        } else if (newIndex > lastIndex) {
            ByteBuf request = Unpooled.buffer(12);
            request.writeShortLE(MSG_LOG_SYNC);
            request.writeShortLE(4);
            request.writeIntLE((int) lastIndex);
            request.writeIntLE(0);
            channel.writeAndFlush(new NetworkMessage(request, channel.remoteAddress()));
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        int type = buf.readUnsignedShortLE();
        buf.readUnsignedShortLE(); // length

        if (type == MSG_IDENT || type == MSG_IDENT_FULL) {

            buf.readUnsignedIntLE(); // id
            int length = buf.readUnsignedShortLE();
            buf.skipBytes(length);
            length = buf.readUnsignedShortLE();
            buf.skipBytes(length);
            length = buf.readUnsignedShortLE();
            String imei = buf.readSlice(length).toString(StandardCharsets.US_ASCII);
            getDeviceSession(channel, remoteAddress, imei);

        } else if (type == MSG_POINT || type == MSG_ALARM || type == MSG_LOGMSG) {

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
            if (deviceSession == null) {
                return null;
            }

            List<Position> positions = new LinkedList<>();

            int recordCount = 1;
            if (type == MSG_LOGMSG) {
                recordCount = buf.readUnsignedShortLE();
            }

            for (int j = 0; j < recordCount; j++) {
                Position position = new Position(getProtocolName());
                position.setDeviceId(deviceSession.getDeviceId());

                if (type == MSG_LOGMSG) {
                    position.set(Position.KEY_ARCHIVE, true);
                    int subtype = buf.readUnsignedShortLE();
                    if (subtype == MSG_ALARM) {
                        position.set(Position.KEY_ALARM, Position.ALARM_GENERAL);
                    }
                    if (buf.readUnsignedShortLE() > buf.readableBytes()) {
                        lastIndex += 1;
                        break; // workaround for device bug
                    }
                    lastIndex = buf.readUnsignedIntLE();
                    position.set(Position.KEY_INDEX, lastIndex);
                } else {
                    newIndex = buf.readUnsignedIntLE();
                }

                position.setTime(new Date(buf.readUnsignedIntLE() * 1000));
                position.setLatitude(buf.readIntLE() * 180.0 / 0x7FFFFFFF);
                position.setLongitude(buf.readIntLE() * 180.0 / 0x7FFFFFFF);
                position.setSpeed(buf.readUnsignedIntLE() * 0.01);
                position.setCourse(buf.readUnsignedShortLE() * 0.01);
                position.setAltitude(buf.readUnsignedShortLE() * 0.01);

                int satellites = buf.readUnsignedByte();
                position.setValid(satellites >= 3);
                position.set(Position.KEY_SATELLITES, satellites);

                position.set(Position.KEY_RSSI, buf.readUnsignedByte());
                position.set(Position.KEY_ODOMETER, buf.readUnsignedIntLE());

                long extraFlags = buf.readLongLE();

                if (BitUtil.check(extraFlags, 0)) {
                    int count = buf.readUnsignedShortLE();
                    for (int i = 1; i <= count; i++) {
                        position.set(Position.PREFIX_ADC + i, buf.readUnsignedShortLE());
                    }
                }

                if (BitUtil.check(extraFlags, 1)) {
                    int size = buf.readUnsignedShortLE();
                    position.set("can", buf.toString(buf.readerIndex(), size, StandardCharsets.US_ASCII));
                    buf.skipBytes(size);
                }

                if (BitUtil.check(extraFlags, 2)) {
                    position.set("passenger", ByteBufUtil.hexDump(buf.readSlice(buf.readUnsignedShortLE())));
                }

                if (type == MSG_ALARM) {
                    position.set(Position.KEY_ALARM, true);
                    byte[] response = {(byte) 0xC9, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(response), remoteAddress));
                }

                buf.readUnsignedIntLE(); // crc

                positions.add(position);
            }

            requestArchive(channel);

            return positions;
        }

        return null;
    }

}