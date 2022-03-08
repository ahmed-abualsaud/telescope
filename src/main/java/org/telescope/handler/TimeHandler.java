
package org.telescope.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.telescope.config.Config;
import org.telescope.config.Keys;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.Context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ChannelHandler.Sharable
public class TimeHandler extends ChannelInboundHandlerAdapter {
    
    private final boolean useServerTime;
    private final Set<String> protocols;

    public TimeHandler(Config config) {
        useServerTime = config.getString(Keys.TIME_OVERRIDE).equalsIgnoreCase("serverTime");
        String protocolList = Context.getConfig().getString(Keys.TIME_PROTOCOLS);
        if (protocolList != null) {
            protocols = new HashSet<>(Arrays.asList(protocolList.split("[, ]")));
        } else {
            protocols = null;
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Position && (protocols == null
                || protocols.contains(ctx.pipeline().get(BaseProtocolDecoder.class).getProtocolName()))) {

            Position position = (Position) msg;
            if (useServerTime) {
                position.setDeviceTime(position.getServerTime());
                position.setFixTime(position.getServerTime());
            } else {
                position.setFixTime(position.getDeviceTime());
            }

        }
        ctx.fireChannelRead(msg);
    }

}
