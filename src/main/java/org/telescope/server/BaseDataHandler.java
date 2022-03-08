
package org.telescope.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.telescope.model.Position;

public abstract class BaseDataHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Position) {
            Position position = handlePosition((Position) msg);
            if (position != null) {
                ctx.fireChannelRead(position);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    protected abstract Position handlePosition(Position position);

}
