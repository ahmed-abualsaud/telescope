
package org.telescope.protocol;

import org.telescope.server.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class TramigoFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 20) {
            return null;
        }

        int length;
        if (buf.getUnsignedByte(buf.readerIndex()) == 0x80) {
            length = buf.getUnsignedShortLE(buf.readerIndex() + 6);
        } else {
            length = buf.getUnsignedShort(buf.readerIndex() + 6);
        }

        if (length <= buf.readableBytes()) {
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
