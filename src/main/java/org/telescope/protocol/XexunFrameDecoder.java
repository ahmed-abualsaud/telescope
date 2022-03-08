
package org.telescope.protocol;

import org.telescope.javel.framework.helper.BufferUtil;
import org.telescope.server.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class XexunFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 80) {
            return null;
        }

        int beginIndex = BufferUtil.indexOf("GPRMC", buf);
        if (beginIndex == -1) {
            beginIndex = BufferUtil.indexOf("GNRMC", buf);
            if (beginIndex == -1) {
                return null;
            }
        }

        int identifierIndex = BufferUtil.indexOf("imei:", buf, beginIndex, buf.writerIndex());
        if (identifierIndex == -1) {
            return null;
        }

        int endIndex = buf.indexOf(identifierIndex, buf.writerIndex(), (byte) ',');
        if (endIndex == -1) {
            return null;
        }

        buf.skipBytes(beginIndex - buf.readerIndex());

        return buf.readRetainedSlice(endIndex - beginIndex + 1);
    }

}
