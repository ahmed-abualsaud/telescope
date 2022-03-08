
package org.telescope.handler.events;

import io.netty.channel.ChannelHandler;
import org.telescope.database.IdentityManager;
import org.telescope.model.Device;
import org.telescope.model.Event;
import org.telescope.model.Position;

import java.util.Collections;
import java.util.Map;

@ChannelHandler.Sharable
public class FuelDropEventHandler extends BaseEventHandler {

    public static final String ATTRIBUTE_FUEL_DROP_THRESHOLD = "fuelDropThreshold";

    private final IdentityManager identityManager;

    public FuelDropEventHandler(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {

        Device device = identityManager.getById(position.getDeviceId());
        if (device == null) {
            return null;
        }
        if (!identityManager.isLatestPosition(position)) {
            return null;
        }

        double fuelDropThreshold = identityManager
                .lookupAttributeDouble(device.getId(), ATTRIBUTE_FUEL_DROP_THRESHOLD, 0, true, false);

        if (fuelDropThreshold > 0) {
            Position lastPosition = identityManager.getLastPosition(position.getDeviceId());
            if (position.getAttributes().containsKey(Position.KEY_FUEL_LEVEL)
                    && lastPosition != null && lastPosition.getAttributes().containsKey(Position.KEY_FUEL_LEVEL)) {

                double drop = lastPosition.getDouble(Position.KEY_FUEL_LEVEL)
                        - position.getDouble(Position.KEY_FUEL_LEVEL);
                if (drop >= fuelDropThreshold) {
                    Event event = new Event(Event.TYPE_DEVICE_FUEL_DROP, position);
                    event.set(ATTRIBUTE_FUEL_DROP_THRESHOLD, fuelDropThreshold);
                    return Collections.singletonMap(event, position);
                }
            }
        }

        return null;
    }

}
