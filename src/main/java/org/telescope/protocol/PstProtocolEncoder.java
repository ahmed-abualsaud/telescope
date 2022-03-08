
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.telescope.javel.framework.helper.Checksum;
import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Protocol;

public class PstProtocolEncoder extends BaseProtocolEncoder {

    public PstProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    private ByteBuf encodeContent(long deviceId, int type, int data1, int data2) {

        ByteBuf buf = Unpooled.buffer();

        buf.writeInt((int) Long.parseLong(getUniqueId(deviceId)));
        buf.writeByte(0x06); // version

        buf.writeInt(1); // index
        buf.writeByte(PstProtocolDecoder.MSG_COMMAND);
        buf.writeShort(type);
        buf.writeShort(data1);
        buf.writeShort(data2);

        buf.writeShort(Checksum.crc16(Checksum.CRC16_XMODEM, buf.nioBuffer()));

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_ENGINE_STOP:
                return encodeContent(command.getDeviceId(), 0x0002, 0xffff, 0xffff);
            case Command.TYPE_ENGINE_RESUME:
                return encodeContent(command.getDeviceId(), 0x0001, 0xffff, 0xffff);
            default:
                return null;
        }
    }

}
