
package org.telescope.protocol;

import org.telescope.server.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class RetranslatorFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int length = 4 + buf.getIntLE(buf.readerIndex());
        if (buf.readableBytes() >= length) {
            return buf.readRetainedSlice(length);
        } else {
            return null;
        }
    }

}
