
package org.telescope.protocol;

import org.telescope.server.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class AdmFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 15 + 3) {
            return null;
        }

        int length;
        if (Character.isDigit(buf.getUnsignedByte(buf.readerIndex()))) {
            length = 15 + buf.getUnsignedByte(buf.readerIndex() + 15 + 2);
        } else {
            length = buf.getUnsignedByte(buf.readerIndex() + 2);
        }

        if (buf.readableBytes() >= length) {
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
