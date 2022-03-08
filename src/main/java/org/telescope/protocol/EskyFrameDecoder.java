
package org.telescope.protocol;

import org.telescope.server.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class EskyFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int startIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) 'E');
        if (startIndex >= 0) {
            buf.readerIndex(startIndex);
            int endIndex = buf.indexOf(buf.readerIndex() + 1, buf.writerIndex(), (byte) 'E');
            if (endIndex > 0) {
                return buf.readRetainedSlice(endIndex - buf.readerIndex());
            } else {
                return buf.readRetainedSlice(buf.readableBytes()); // assume full frame
            }
        }

        return null;
    }

}
