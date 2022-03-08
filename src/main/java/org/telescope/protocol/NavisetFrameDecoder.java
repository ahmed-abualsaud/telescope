
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import org.telescope.javel.framework.helper.BitUtil;
import org.telescope.server.BaseFrameDecoder;

public class NavisetFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int length = 2 + BitUtil.to(buf.getUnsignedShortLE(buf.readerIndex()), 12) + 2;

        if (buf.readableBytes() >= length) {
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
