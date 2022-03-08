
package org.telescope.protocol;

import org.telescope.server.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class SuntechFrameDecoder extends BaseFrameDecoder {

    private ByteBuf readFrame(ByteBuf buf, int delimiterIndex) {
        ByteBuf frame = buf.readRetainedSlice(delimiterIndex - buf.readerIndex());
        buf.skipBytes(1); // delimiter
        return frame;
    }

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.getByte(buf.readerIndex() + 1) == 0) {

            int length = 1 + 2 + buf.getShort(buf.readerIndex() + 1);
            if (buf.readableBytes() >= length) {
                return buf.readRetainedSlice(length);
            }

        } else {

            int delimiterIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '\r');
            while (delimiterIndex > 0) {
                if (delimiterIndex + 1 < buf.writerIndex() && buf.getByte(delimiterIndex + 1) == '\n') {
                    delimiterIndex = buf.indexOf(delimiterIndex + 1, buf.writerIndex(), (byte) '\r');
                } else {
                    return readFrame(buf, delimiterIndex);
                }
            }

        }

        return null;
    }

}
