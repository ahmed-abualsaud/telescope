
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.telescope.javel.framework.helper.Checksum;
import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Context;
import org.telescope.server.Protocol;

import java.nio.charset.StandardCharsets;

public class CastelProtocolEncoder extends BaseProtocolEncoder {

    public CastelProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    private ByteBuf encodeContent(long deviceId, short type, ByteBuf content) {

        ByteBuf buf = Unpooled.buffer(0);
        String uniqueId = Context.getIdentityManager().getById(deviceId).getUniqueId();

        buf.writeByte('@');
        buf.writeByte('@');

        buf.writeShortLE(2 + 2 + 1 + 20 + 2 + content.readableBytes() + 2 + 2); // length

        buf.writeByte(1); // protocol version

        buf.writeBytes(uniqueId.getBytes(StandardCharsets.US_ASCII));
        buf.writeZero(20 - uniqueId.length());

        buf.writeShort(type);
        buf.writeBytes(content);

        buf.writeShortLE(Checksum.crc16(Checksum.CRC16_X25, buf.nioBuffer()));

        buf.writeByte('\r');
        buf.writeByte('\n');

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {
        ByteBuf content = Unpooled.buffer(0);
        switch (command.getType()) {
            case Command.TYPE_ENGINE_STOP:
                content.writeByte(1);
                return encodeContent(command.getDeviceId(), CastelProtocolDecoder.MSG_CC_PETROL_CONTROL, content);
            case Command.TYPE_ENGINE_RESUME:
                content.writeByte(0);
                return encodeContent(command.getDeviceId(), CastelProtocolDecoder.MSG_CC_PETROL_CONTROL, content);
            default:
                return null;
        }
    }

}
