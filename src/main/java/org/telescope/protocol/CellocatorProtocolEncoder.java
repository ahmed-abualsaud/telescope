
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Protocol;

public class CellocatorProtocolEncoder extends BaseProtocolEncoder {

    public CellocatorProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    public static ByteBuf encodeContent(int type, int uniqueId, int packetNumber, ByteBuf content) {

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte('M');
        buf.writeByte('C');
        buf.writeByte('G');
        buf.writeByte('P');
        buf.writeByte(type);
        buf.writeIntLE(uniqueId);
        buf.writeByte(packetNumber);
        buf.writeIntLE(0); // authentication code
        buf.writeBytes(content);

        byte checksum = 0;
        for (int i = 4; i < buf.writerIndex(); i++) {
            checksum += buf.getByte(i);
        }
        buf.writeByte(checksum);

        return buf;
    }

    private ByteBuf encodeCommand(long deviceId, int command, int data1, int data2) {

        ByteBuf content = Unpooled.buffer();
        content.writeByte(command);
        content.writeByte(command);
        content.writeByte(data1);
        content.writeByte(data1);
        content.writeByte(data2);
        content.writeByte(data2);
        content.writeIntLE(0); // command specific data

        ByteBuf buf = encodeContent(0, Integer.parseInt(getUniqueId(deviceId)), 0, content);
        content.release();

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_OUTPUT_CONTROL:
                int data = Integer.parseInt(command.getString(Command.KEY_DATA)) << 4
                        + command.getInteger(Command.KEY_INDEX);
                return encodeCommand(command.getDeviceId(), 0x03, data, 0);
            default:
                return null;
        }
    }

}
