
package org.telescope.protocol;

import org.telescope.server.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class StbFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int startIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '{');
        if (startIndex >= 0) {

            buf.readerIndex(startIndex);

            int currentIndex = startIndex + 1;
            int nesting = 1;
            while (currentIndex < buf.writerIndex() && nesting > 0) {
                byte currentByte = buf.getByte(currentIndex);
                if (currentByte == '{') {
                    nesting += 1;
                } else if (currentByte == '}') {
                    nesting -= 1;
                }
                currentIndex += 1;
            }

            if (nesting == 0) {
                return buf.readRetainedSlice(currentIndex - startIndex);
            }

        }

        return null;
    }

}
