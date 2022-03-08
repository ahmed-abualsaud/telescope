
package org.telescope.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.config.Config;
import org.telescope.config.Keys;
import org.telescope.database.StatisticsManager;
import org.telescope.javel.framework.service.geolocation.GeolocationProvider;
import org.telescope.model.Position;

@ChannelHandler.Sharable
public class GeolocationHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeolocationHandler.class);

    private final GeolocationProvider geolocationProvider;
    private final StatisticsManager statisticsManager;
    private final boolean processInvalidPositions;

    public GeolocationHandler(
            Config config, GeolocationProvider geolocationProvider, StatisticsManager statisticsManager) {
        this.geolocationProvider = geolocationProvider;
        this.statisticsManager = statisticsManager;
        this.processInvalidPositions = config.getBoolean(Keys.GEOLOCATION_PROCESS_INVALID_POSITIONS);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object message) {
        if (message instanceof Position) {
            final Position position = (Position) message;
            if ((position.getOutdated() || processInvalidPositions && !position.getValid())
                    && position.getNetwork() != null) {
                if (statisticsManager != null) {
                    statisticsManager.registerGeolocationRequest();
                }

                geolocationProvider.getLocation(position.getNetwork(),
                        new GeolocationProvider.LocationProviderCallback() {
                    @Override
                    public void onSuccess(double latitude, double longitude, double accuracy) {
                        position.set(Position.KEY_APPROXIMATE, true);
                        position.setValid(true);
                        position.setFixTime(position.getDeviceTime());
                        position.setLatitude(latitude);
                        position.setLongitude(longitude);
                        position.setAccuracy(accuracy);
                        position.setAltitude(0);
                        position.setSpeed(0);
                        position.setCourse(0);
                        ctx.fireChannelRead(position);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        LOGGER.warn("Geolocation network error", e);
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
