package org.telescope.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.config.Config;
import org.telescope.config.Keys;
import org.telescope.database.IdentityManager;
import org.telescope.javel.framework.service.geocoder.Geocoder;
import org.telescope.model.Position;
import org.telescope.server.Context;

@ChannelHandler.Sharable
public class GeocoderHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeocoderHandler.class);

    private final Geocoder geocoder;
    private final IdentityManager identityManager;
    private final boolean ignorePositions;
    private final boolean processInvalidPositions;
    private final int geocoderReuseDistance;

    public GeocoderHandler(
            Config config, Geocoder geocoder, IdentityManager identityManager) {
        this.geocoder = geocoder;
        this.identityManager = identityManager;
        ignorePositions = Context.getConfig().getBoolean(Keys.GEOCODER_IGNORE_POSITIONS);
        processInvalidPositions = config.getBoolean(Keys.GEOCODER_PROCESS_INVALID_POSITIONS);
        geocoderReuseDistance = config.getInteger(Keys.GEOCODER_REUSE_DISTANCE, 0);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object message) {
        if (message instanceof Position && !ignorePositions) {
            final Position position = (Position) message;
            if (processInvalidPositions || position.getValid()) {
                if (geocoderReuseDistance != 0) {
                    Position lastPosition = identityManager.getLastPosition(position.getDeviceId());
                    if (lastPosition != null && lastPosition.getAddress() != null
                            && position.getDouble(Position.KEY_DISTANCE) <= geocoderReuseDistance) {
                        position.setAddress(lastPosition.getAddress());
                        ctx.fireChannelRead(position);
                        return;
                    }
                }

                geocoder.getAddress(position.getLatitude(), position.getLongitude(),
                        new Geocoder.ReverseGeocoderCallback() {
                    @Override
                    public void onSuccess(String address) {
                        position.setAddress(address);
                        ctx.fireChannelRead(position);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        LOGGER.warn("Geocoding failed", e);
                        ctx.fireChannelRead(position);
                    }
                });
            } else {
                ctx.fireChannelRead(position);
            }
        } else {
            ctx.fireChannelRead(message);
        }
    }

}
