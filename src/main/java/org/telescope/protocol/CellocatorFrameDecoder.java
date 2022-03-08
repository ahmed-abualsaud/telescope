
package org.telescope.protocol;

import org.telescope.server.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class CellocatorFrameDecoder extends BaseFrameDecoder {

    private static final int MESSAGE_MINIMUM_LENGTH = 15;

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < MESSAGE_MINIMUM_LENGTH) {
            return null;
        }

        int length = 0;
        int type = buf.getUnsignedByte(4);
        switch (type) {
            case CellocatorProtocolDecoder.MSG_CLIENT_STATUS:
                length = 70;
                break;
            case CellocatorProtocolDecoder.MSG_CLIENT_PROGRAMMING:
                length = 31;
                break;
            case CellocatorProtocolDecoder.MSG_CLIENT_SERIAL_LOG:
                length = 70;
                break;
            case CellocatorProtocolDecoder.MSG_CLIENT_SERIAL:
                if (buf.readableBytes() >= 19) {
                    length = 19 + buf.getUnsignedShortLE(buf.readerIndex() + 16);
                }
                break;
            case CellocatorProtocolDecoder.MSG_CLIENT_MODULAR:
                length = 15 + buf.getUnsignedByte(buf.readerIndex() + 13);
                break;
            case CellocatorProtocolDecoder.MSG_CLIENT_MODULAR_EXT:
                length = 16 + buf.getUnsignedShortLE(buf.readerIndex() + 13);
                break;
            default:
                break;
        }

        if (length > 0 && buf.readableBytes() >= length) {
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
