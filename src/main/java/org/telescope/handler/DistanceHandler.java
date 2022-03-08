
package org.telescope.handler;

import io.netty.channel.ChannelHandler;

import org.telescope.config.Config;
import org.telescope.config.Keys;
import org.telescope.database.IdentityManager;
import org.telescope.javel.framework.helper.DistanceCalculator;
import org.telescope.model.Position;
import org.telescope.server.BaseDataHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;

@ChannelHandler.Sharable
public class DistanceHandler extends BaseDataHandler {

    private final IdentityManager identityManager;

    private final boolean filter;
    private final int coordinatesMinError;
    private final int coordinatesMaxError;

    public DistanceHandler(Config config, IdentityManager identityManager) {
        this.identityManager = identityManager;
        this.filter = config.getBoolean(Keys.COORDINATES_FILTER);
        this.coordinatesMinError = config.getInteger(Keys.COORDINATES_MIN_ERROR);
        this.coordinatesMaxError = config.getInteger(Keys.COORDINATES_MAX_ERROR);
    }

    @Override
    protected Position handlePosition(Position position) {

        double distance = 0.0;
        if (position.getAttributes().containsKey(Position.KEY_DISTANCE)) {
            distance = position.getDouble(Position.KEY_DISTANCE);
        }
        double totalDistance = 0.0;

        Position last = identityManager != null ? identityManager.getLastPosition(position.getDeviceId()) : null;
        if (last != null) {
            totalDistance = last.getDouble(Position.KEY_TOTAL_DISTANCE);
            if (!position.getAttributes().containsKey(Position.KEY_DISTANCE)) {
                distance = DistanceCalculator.distance(
                        position.getLatitude(), position.getLongitude(),
                        last.getLatitude(), last.getLongitude());
                distance = BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            }
            if (filter && last.getLatitude() != 0 && last.getLongitude() != 0) {
                boolean satisfiesMin = coordinatesMinError == 0 || distance > coordinatesMinError;
                boolean satisfiesMax = coordinatesMaxError == 0 || distance < coordinatesMaxError;
                if (!satisfiesMin || !satisfiesMax) {
                    position.setValid(last.getValid());
                    position.setLatitude(last.getLatitude());
                    position.setLongitude(last.getLongitude());
                    distance = 0;
                }
            }
        }
        position.set(Position.KEY_DISTANCE, distance);
        totalDistance = BigDecimal.valueOf(totalDistance + distance).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        position.set(Position.KEY_TOTAL_DISTANCE, totalDistance);

        return position;
    }

}
