
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.telescope.javel.framework.helper.Checksum;
import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Protocol;

public class BceProtocolEncoder extends BaseProtocolEncoder {

    public BceProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        if (command.getType().equals(Command.TYPE_OUTPUT_CONTROL)) {
            ByteBuf buf = Unpooled.buffer();

            buf.writeLongLE(Long.parseLong(getUniqueId(command.getDeviceId())));
            buf.writeShortLE(1 + 1 + 3 + 1); // length
            buf.writeByte(BceProtocolDecoder.MSG_OUTPUT_CONTROL);
            buf.writeByte(command.getInteger(Command.KEY_INDEX) == 1 ? 0x0A : 0x0B);
            buf.writeByte(0xFF); // index
            buf.writeByte(0x00); // form id
            buf.writeShortLE(Integer.parseInt(command.getString(Command.KEY_DATA)) > 0 ? 0x0055 : 0x0000);
            buf.writeByte(Checksum.sum(buf.nioBuffer()));

            return buf;
        } else {
            return null;
        }
    }

}
