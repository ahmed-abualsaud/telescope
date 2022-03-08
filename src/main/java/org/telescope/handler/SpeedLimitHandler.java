
package org.telescope.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.model.Position;
import org.telescope.speedlimit.SpeedLimitProvider;

@ChannelHandler.Sharable
public class SpeedLimitHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpeedLimitHandler.class);

    private final SpeedLimitProvider speedLimitProvider;

    public SpeedLimitHandler(SpeedLimitProvider speedLimitProvider) {
        this.speedLimitProvider = speedLimitProvider;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object message) {
        if (message instanceof Position) {
            final Position position = (Position) message;
            speedLimitProvider.getSpeedLimit(position.getLatitude(), position.getLongitude(),
                    new SpeedLimitProvider.SpeedLimitProviderCallback() {
                @Override
                public void onSuccess(double speedLimit) {
                    position.set(Position.KEY_SPEED_LIMIT, speedLimit);
                    ctx.fireChannelRead(position);
                }

                @Override
                public void onFailure(Throwable e) {
                    LOGGER.warn("Speed limit provider failed", e);
                    ctx.fireChannelRead(position);
                }
            });
        } else {
            ctx.fireChannelRead(message);
        }
    }

}
