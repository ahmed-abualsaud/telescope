
package org.telescope.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.server.NetworkMessage;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class StandardLoggingHandler extends ChannelDuplexHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardLoggingHandler.class);

    private final String protocol;

    public StandardLoggingHandler(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log(ctx, false, msg);
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log(ctx, true, msg);
        super.write(ctx, msg, promise);
    }

    public void log(ChannelHandlerContext ctx, boolean downstream, Object o) {
        if (o instanceof NetworkMessage) {
            NetworkMessage networkMessage = (NetworkMessage) o;
            if (networkMessage.getMessage() instanceof ByteBuf) {
                log(ctx, downstream, networkMessage.getRemoteAddress(), (ByteBuf) networkMessage.getMessage());
            }
        } else if (o instanceof ByteBuf) {
            log(ctx, downstream, ctx.channel().remoteAddress(), (ByteBuf) o);
        }
    }

    public void log(ChannelHandlerContext ctx, boolean downstream, SocketAddress remoteAddress, ByteBuf buf) {
        StringBuilder message = new StringBuilder();

        message.append("[").append(ctx.channel().id().asShortText()).append(": ");
        message.append(protocol);
        if (downstream) {
            message.append(" > ");
        } else {
            message.append(" < ");
        }

        if (remoteAddress instanceof InetSocketAddress) {
            message.append(((InetSocketAddress) remoteAddress).getHostString());
        } else {
            message.append("unknown");
        }
        message.append("]");

        message.append(" HEX: ");
        message.append(ByteBufUtil.hexDump(buf));

        LOGGER.info(message.toString());
    }

}
