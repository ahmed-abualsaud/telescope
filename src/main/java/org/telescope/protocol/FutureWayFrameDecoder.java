
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import org.telescope.javel.framework.helper.DataConverter;
import org.telescope.server.BaseFrameDecoder;

import java.nio.charset.StandardCharsets;

public class FutureWayFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 10) {
            return null;
        }

        int length = Unpooled.wrappedBuffer(DataConverter.parseHex(
                buf.getCharSequence(buf.readerIndex() + 2, 8, StandardCharsets.US_ASCII).toString())).readInt() + 17;

        if (buf.readableBytes() >= length) {
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
