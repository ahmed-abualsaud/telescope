
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import org.telescope.javel.framework.helper.BufferUtil;
import org.telescope.server.BaseFrameDecoder;

public class ArnaviFrameDecoder extends BaseFrameDecoder {

    private static final int HEADER_LENGTH = 10;
    private static final int PACKET_WRAPPER_LENGTH = 8;
    private static final int RESULT_TYPE = 0xfd;
    private static final byte PACKAGE_END_SIGN = 0x5d;

    private boolean firstPacket = true;

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 4) {
            return null;
        }

        if (buf.getByte(buf.readerIndex()) == '$') {

            int index = BufferUtil.indexOf("\r\n", buf);
            if (index > 0) {
                ByteBuf frame = buf.readRetainedSlice(index - buf.readerIndex());
                buf.skipBytes(2);
                return frame;
            }

        } else {

            int length;
            if (firstPacket) {
                firstPacket = false;
                length = HEADER_LENGTH;
            } else {
                int type = buf.getUnsignedByte(1);
                if (type == RESULT_TYPE) {
                    length = 4;
                } else {
                    int index = 2;
                    while (index + PACKET_WRAPPER_LENGTH < buf.readableBytes()
                            && buf.getByte(index) != PACKAGE_END_SIGN) {
                        index += PACKET_WRAPPER_LENGTH + buf.getUnsignedShortLE(index + 1);
                    }
                    if (buf.getByte(index) != PACKAGE_END_SIGN) {
                        return null;
                    }
                    length = index + 1;
                }
            }

            if (buf.readableBytes() >= length) {
                return buf.readRetainedSlice(length);
            }

        }

        return null;
    }

}
