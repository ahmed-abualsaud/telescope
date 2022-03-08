
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PstFrameEncoder extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {

        out.writeByte('(');
        while (msg.isReadable()) {
            int b = msg.readUnsignedByte();
            if (b == 0x27 || b == 0x28 || b == 0x29) {
                out.writeByte(0x27);
                out.writeByte(b ^ 0x40);
            } else {
                out.writeByte(b);
            }
        }
        out.writeByte(')');
    }
}
