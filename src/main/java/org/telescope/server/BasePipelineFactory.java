package org.telescope.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

import org.telescope.config.Keys;
import org.telescope.server.handler.MainHandler;

import org.telescope.handler.ComputedAttributesHandler;
import org.telescope.handler.CopyAttributesHandler;
import org.telescope.handler.DefaultDataHandler;
import org.telescope.handler.DistanceHandler;
import org.telescope.handler.EngineHoursHandler;
import org.telescope.handler.FilterHandler;
import org.telescope.handler.GeocoderHandler;
import org.telescope.handler.GeolocationHandler;
import org.telescope.handler.HemisphereHandler;
import org.telescope.handler.MotionHandler;
import org.telescope.handler.NetworkMessageHandler;
import org.telescope.handler.OpenChannelHandler;
import org.telescope.handler.RemoteAddressHandler;
import org.telescope.handler.SpeedLimitHandler;
import org.telescope.handler.StandardLoggingHandler;
import org.telescope.handler.TimeHandler;
import org.telescope.handler.events.AlertEventHandler;
import org.telescope.handler.events.BehaviorEventHandler;
import org.telescope.handler.events.CommandResultEventHandler;
import org.telescope.handler.events.DriverEventHandler;
import org.telescope.handler.events.FuelDropEventHandler;
import org.telescope.handler.events.GeofenceEventHandler;
import org.telescope.handler.events.IgnitionEventHandler;
import org.telescope.handler.events.MaintenanceEventHandler;
import org.telescope.handler.events.MotionEventHandler;
import org.telescope.handler.events.OverspeedEventHandler;

import java.util.Map;

public abstract class BasePipelineFactory extends ChannelInitializer<Channel> {

    private final TrackerServer server;
    private final String protocol;
    private int timeout;

    public BasePipelineFactory(TrackerServer server, String protocol) {
        this.server = server;
        this.protocol = protocol;
        timeout = Context.getConfig().getInteger(Keys.PROTOCOL_TIMEOUT.withPrefix(protocol));
        if (timeout == 0) {
            timeout = Context.getConfig().getInteger(Keys.SERVER_TIMEOUT);
        }
    }

    protected abstract void addProtocolHandlers(PipelineBuilder pipeline);

    @SafeVarargs
    private final void addHandlers(ChannelPipeline pipeline, Class<? extends ChannelHandler>... handlerClasses) {
        for (Class<? extends ChannelHandler> handlerClass : handlerClasses) {
            if (handlerClass != null) {
                pipeline.addLast(Main.getInjector().getInstance(handlerClass));
            }
        }
    }

    public static <T extends ChannelHandler> T getHandler(ChannelPipeline pipeline, Class<T> clazz) {
        for (Map.Entry<String, ChannelHandler> handlerEntry : pipeline) {
            ChannelHandler handler = handlerEntry.getValue();
            if (handler instanceof WrapperInboundHandler) {
                handler = ((WrapperInboundHandler) handler).getWrappedHandler();
            } else if (handler instanceof WrapperOutboundHandler) {
                handler = ((WrapperOutboundHandler) handler).getWrappedHandler();
            }
            if (clazz.isAssignableFrom(handler.getClass())) {
                return (T) handler;
            }
        }
        return null;
    }

    @Override
    protected void initChannel(Channel channel) {
        final ChannelPipeline pipeline = channel.pipeline();

        if (timeout > 0 && !server.isDatagram()) {
            pipeline.addLast(new IdleStateHandler(timeout, 0, 0));
        }
        pipeline.addLast(new OpenChannelHandler(server));
        pipeline.addLast(new NetworkMessageHandler());
        pipeline.addLast(new StandardLoggingHandler(protocol));

        addProtocolHandlers(handler -> {
            if (!(handler instanceof BaseProtocolDecoder || handler instanceof BaseProtocolEncoder)) {
                if (handler instanceof ChannelInboundHandler) {
                    handler = new WrapperInboundHandler((ChannelInboundHandler) handler);
                } else {
                    handler = new WrapperOutboundHandler((ChannelOutboundHandler) handler);
                }
            }
            pipeline.addLast(handler);
        });
        pipeline.addLast(new MainHandler());

        /*addHandlers(
                pipeline,
                TimeHandler.class,
                GeolocationHandler.class,
                HemisphereHandler.class,
                DistanceHandler.class,
                RemoteAddressHandler.class,
                FilterHandler.class,
                GeocoderHandler.class,
                SpeedLimitHandler.class,
                MotionHandler.class,
                CopyAttributesHandler.class,
                EngineHoursHandler.class,
                ComputedAttributesHandler.class,
                WebDataHandler.class,
                DefaultDataHandler.class,
                CommandResultEventHandler.class,
                OverspeedEventHandler.class,
                BehaviorEventHandler.class,
                FuelDropEventHandler.class,
                MotionEventHandler.class,
                GeofenceEventHandler.class,
                AlertEventHandler.class,
                IgnitionEventHandler.class,
                MaintenanceEventHandler.class,
                DriverEventHandler.class);*/
    }

}
