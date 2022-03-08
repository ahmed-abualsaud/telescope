
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.DataConverter;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.util.Date;

public class DingtekProtocolDecoder extends BaseProtocolDecoder {

    public DingtekProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        int type = Integer.parseInt(sentence.substring(6, 8), 16);

        if (type == 0x01 || type == 0x02 || type == 0x04) {

            ByteBuf buf = Unpooled.wrappedBuffer(DataConverter.parseHex(sentence));

            buf.readUnsignedByte(); // header
            buf.readUnsignedByte(); // forced
            buf.readUnsignedByte(); // device type
            buf.readUnsignedByte(); // type
            buf.readUnsignedByte(); // length

            String imei = ByteBufUtil.hexDump(buf.slice(buf.writerIndex() - 9, 8)).substring(1);
            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
            if (deviceSession == null) {
                return null;
            }

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());
            position.setTime(new Date());

            position.set("height", buf.readUnsignedShort());

            position.setValid(buf.readUnsignedByte() > 0);
            position.setLongitude(buf.readFloat());
            position.setLatitude(buf.readFloat());

            position.set(Position.PREFIX_TEMP + 1, buf.readUnsignedByte());
            position.set(Position.KEY_STATUS, buf.readUnsignedInt());
            position.set(Position.KEY_BATTERY, buf.readUnsignedShort() * 0.001);
            position.set(Position.KEY_RSSI, buf.readFloat());
            position.set(Position.KEY_INDEX, buf.readUnsignedShort());

            return position;
        }

        return null;
    }

}
