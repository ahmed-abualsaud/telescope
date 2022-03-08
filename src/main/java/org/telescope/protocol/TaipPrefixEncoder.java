
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import org.telescope.config.Keys;
import org.telescope.server.Context;
import org.telescope.server.Protocol;

import java.util.List;

@ChannelHandler.Sharable
public class TaipPrefixEncoder extends MessageToMessageEncoder<ByteBuf> {

    private final Protocol protocol;

    public TaipPrefixEncoder(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        if (Context.getConfig().getBoolean(Keys.PROTOCOL_PREFIX.withPrefix(protocol.getName()))) {
            out.add(Unpooled.wrappedBuffer(Unpooled.wrappedBuffer(new byte[] {0x20, 0x20, 0x06, 0x00}), msg.retain()));
        } else {
            out.add(msg.retain());
        }
    }

}
