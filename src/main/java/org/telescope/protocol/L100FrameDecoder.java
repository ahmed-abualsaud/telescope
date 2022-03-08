
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

import org.telescope.server.BaseFrameDecoder;

public class L100FrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 10) {
            return null;
        }

        if (buf.getCharSequence(buf.readerIndex(), 4, StandardCharsets.US_ASCII).toString().equals("ATL,")) {
            return decodeNew(buf);
        } else {
            return decodeOld(buf);
        }
    }

    private Object decodeOld(ByteBuf buf) {

        int header = buf.getByte(buf.readerIndex());
        boolean obd = header == 'L' || header == 'H';

        int index;
        if (obd) {
            index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '*');
        } else {
            index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) 0x02);
            if (index < 0) {
                index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) 0x04);
                if (index < 0) {
                    return null;
                }
            }
        }

        index += 2; // checksum

        if (buf.writerIndex() >= index) {
            if (!obd) {
                buf.skipBytes(2); // header
            }
            ByteBuf frame = buf.readRetainedSlice(index - buf.readerIndex() - 2);
            buf.skipBytes(2); // footer
            return frame;
        }

        return null;
    }

    private Object decodeNew(ByteBuf buf) {

        int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '@');
        if (index < 0) {
            return null;
        }

        if (buf.writerIndex() >= index + 1) {
            ByteBuf frame = buf.readRetainedSlice(index - buf.readerIndex());
            buf.skipBytes(1); // delimiter
            return frame;
        }

        return null;
    }

}
