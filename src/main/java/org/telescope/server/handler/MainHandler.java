package org.telescope.server.handler;

import org.telescope.config.Keys;
import org.telescope.javel.framework.helper.JSON;
import org.telescope.javel.framework.storage.database.DB;
import org.telescope.server.Context;
import org.telescope.server.BasePipelineFactory;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.app.event.DeviceUpdateState;


import java.util.Set;
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.ChannelInboundHandlerAdapter;

import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.codec.http.HttpRequestDecoder;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainHandler.class);

    private String channel;
    private Map<String, Object> data;
    private final Set<String> connectionlessProtocols = new HashSet<>();
    private final Set<String> logAttributes = new LinkedHashSet<>();
    
    public MainHandler() {
        logAttributes.addAll(Arrays.asList(Context.getConfig().getString(Keys.LOGGER_ATTRIBUTES).split("[, ]")));
        String connectionlessProtocolList = Context.getConfig().getString(Keys.STATUS_IGNORE_OFFLINE);
        if (connectionlessProtocolList != null) {
            connectionlessProtocols.addAll(Arrays.asList(connectionlessProtocolList.split("[, ]")));
        }
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        handle(message);
        Context.event(new DeviceUpdateState(channel, data));
        ctx.fireChannelRead(message);
    }
    
    private void handle(Object message) {
        Map<String, Object> state = JSON.decode(message);
        Map<String, Object> newPosition = new HashMap<>();
        Map<String, Object> attributes = JSON.decode(state.get("attributes"));
        Map<String, Object> oldPosition = DB.table("positions")
            .select("latitude", "longitude", "device_id", "user_id", "fix_time", 
                    "engine", "speed", "acceleration", "duration", "distance",
                    "count", "battery", "motion", "positions.attributes")
            .join("devices", "positions.id", "=", "devices.position_id")
            .where("device_id", state.get("deviceId"))
            .first();

        AlarmHandler.handle(attributes, newPosition);
        PositionHandler.handle(state, attributes, oldPosition, newPosition);
        DurationHandler.handle(state, oldPosition, newPosition);
        MotionHandler.handle(state, attributes, oldPosition, newPosition);
        DistanceHandler.handle(state, attributes, oldPosition, newPosition);
        SpeedHandler.handle(state, oldPosition, newPosition);
        AccelerationHandler.handle(state, attributes, oldPosition, newPosition);
        EngineHandler.handle(attributes, oldPosition, newPosition);
        BatteryHandler.handle(attributes, oldPosition, newPosition);
        GeocoderHandler.handle(state, newPosition);

        this.data = new HashMap<>();
        this.data.putAll(newPosition);
        if (((Boolean) state.get("valid"))) {
            state = DB.table("positions").create(newPosition);
            Map<String, Object> input = new HashMap<>();
            input.put("position_id", state.get("id"));
            DB.table("devices").where("id", state.get("device_id")).update(input);
        }
        if (oldPosition != null) {
            this.channel = "user." + oldPosition.get("user_id") + ".devices";
        }
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

    private static String formatChannel(Channel channel) {
        return String.format("[%s]", channel.id().asShortText());
    }
}
