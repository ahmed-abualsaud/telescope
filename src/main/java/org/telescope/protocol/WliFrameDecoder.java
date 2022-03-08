
package org.telescope.protocol;

import org.telescope.server.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class WliFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 2) {
            return null;
        }

        int index = buf.indexOf(buf.readerIndex() + 1, buf.writerIndex(), (byte) 0x03);
        if (index != -1) {
            ByteBuf result = Unpooled.buffer(index + 1 - buf.readerIndex());

            while (buf.readerIndex() <= index) {
                int b = buf.readUnsignedByte();
                if (b == 0xDB) {
                    int ext = buf.readUnsignedByte();
                    if (ext == 0xD2) {
                        result.writeByte(0x02);
                    } else if (ext == 0xD3) {
                        result.writeByte(0x03);
                    } else if (ext == 0xDD) {
                        result.writeByte(0xDB);
                    }
                } else {
                    result.writeByte(b);
                }
            }

            return result;
        }

        return null;
    }

}
