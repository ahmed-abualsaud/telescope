
package org.telescope.protocol;

import org.telescope.javel.framework.helper.Checksum;
import org.telescope.javel.framework.helper.DataConverter;
import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class TeltonikaProtocolEncoder extends BaseProtocolEncoder {

    public TeltonikaProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    private ByteBuf encodeContent(byte[] content) {

        ByteBuf buf = Unpooled.buffer();

        buf.writeInt(0);
        buf.writeInt(content.length + 8);
        buf.writeByte(TeltonikaProtocolDecoder.CODEC_12);
        buf.writeByte(1); // quantity
        buf.writeByte(5); // type
        buf.writeInt(content.length);
        buf.writeBytes(content);
        buf.writeByte(1); // quantity
        buf.writeInt(Checksum.crc16(Checksum.CRC16_IBM, buf.nioBuffer(8, buf.writerIndex() - 8)));

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        if (command.getType().equals(Command.TYPE_CUSTOM)) {
            String data = command.getString(Command.KEY_DATA);
            if (data.matches("(\\p{XDigit}{2})+")) {
                return encodeContent(DataConverter.parseHex(data));
            } else {
                return encodeContent((data + "\r\n").getBytes(StandardCharsets.US_ASCII));
            }
        } else {
            return null;
        }
    }

}
