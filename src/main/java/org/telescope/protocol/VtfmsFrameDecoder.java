
package org.telescope.protocol;

import org.telescope.server.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class VtfmsFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) ')');
        if (endIndex > 0) {
            endIndex += 1 + 3;
            if (buf.writerIndex() >= endIndex) {
                return buf.readRetainedSlice(endIndex - buf.readerIndex());
            }
        }

        return null;
    }

}
