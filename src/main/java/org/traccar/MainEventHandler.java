package org.traccar;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.traccar.config.Keys;
import org.traccar.api.event.DeviceUpdateState;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class MainEventHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainEventHandler.class);

    private final Set<String> connectionlessProtocols = new HashSet<>();
    private final Set<String> logAttributes = new LinkedHashSet<>();

    public MainEventHandler() {
        String connectionlessProtocolList = Context.getConfig().getString(Keys.STATUS_IGNORE_OFFLINE);
        if (connectionlessProtocolList != null) {
            connectionlessProtocols.addAll(Arrays.asList(connectionlessProtocolList.split("[, ]")));
        }
        logAttributes.addAll(Arrays.asList(
                Context.getConfig().getString(Keys.LOGGER_ATTRIBUTES).split("[, ]")));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        fireUpdateDeviceStateEvent(message);
    }

    private static String formatChannel(Channel channel) {
        return String.format("[%s]", channel.id().asShortText());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (!(ctx.channel() instanceof DatagramChannel)) {
            LOGGER.info(formatChannel(ctx.channel()) + " connected");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.info(formatChannel(ctx.channel()) + " disconnected");
        closeChannel(ctx.channel());

        if (BasePipelineFactory.getHandler(ctx.pipeline(), HttpRequestDecoder.class) == null
                && !connectionlessProtocols.contains(ctx.pipeline().get(BaseProtocolDecoder.class).getProtocolName())) {
            Context.getConnectionManager().removeActiveDevice(ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        LOGGER.warn(formatChannel(ctx.channel()) + " error", cause);
        closeChannel(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            LOGGER.info(formatChannel(ctx.channel()) + " timed out");
            closeChannel(ctx.channel());
        }
    }

    private void closeChannel(Channel channel) {
        if (!(channel instanceof DatagramChannel)) {
            channel.close();
        }
    }

    private synchronized void fireUpdateDeviceStateEvent(Object message) {
        Context.event(new DeviceUpdateState(message));
    }
}
