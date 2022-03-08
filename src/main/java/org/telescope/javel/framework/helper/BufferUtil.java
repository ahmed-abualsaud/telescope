
package org.telescope.javel.framework.helper;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public final class BufferUtil {

    private BufferUtil() {
    }

    public static int indexOf(String needle, ByteBuf haystack) {
        ByteBuf needleBuffer = Unpooled.wrappedBuffer(needle.getBytes(StandardCharsets.US_ASCII));
        try {
            return ByteBufUtil.indexOf(needleBuffer, haystack);
        } finally {
            needleBuffer.release();
        }
    }

    public static int indexOf(String needle, ByteBuf haystack, int startIndex, int endIndex) {
        ByteBuf wrappedHaystack = Unpooled.wrappedBuffer(haystack);
        wrappedHaystack.readerIndex(startIndex - haystack.readerIndex());
        wrappedHaystack.writerIndex(endIndex - haystack.readerIndex());
        int result = indexOf(needle, wrappedHaystack);
        return result < 0 ? result : haystack.readerIndex() + startIndex + result;
    }

}
