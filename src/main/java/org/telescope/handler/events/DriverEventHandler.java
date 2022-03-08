
package org.telescope.handler.events;

import java.util.Collections;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.telescope.database.IdentityManager;
import org.telescope.model.Event;
import org.telescope.model.Position;

@ChannelHandler.Sharable
public class DriverEventHandler extends BaseEventHandler {

    private final IdentityManager identityManager;

    public DriverEventHandler(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        if (!identityManager.isLatestPosition(position)) {
            return null;
        }
        String driverUniqueId = position.getString(Position.KEY_DRIVER_UNIQUE_ID);
        if (driverUniqueId != null) {
            String oldDriverUniqueId = null;
            Position lastPosition = identityManager.getLastPosition(position.getDeviceId());
            if (lastPosition != null) {
                oldDriverUniqueId = lastPosition.getString(Position.KEY_DRIVER_UNIQUE_ID);
            }
            if (!driverUniqueId.equals(oldDriverUniqueId)) {
                Event event = new Event(Event.TYPE_DRIVER_CHANGED, position);
                event.set(Position.KEY_DRIVER_UNIQUE_ID, driverUniqueId);
                return Collections.singletonMap(event, position);
            }
        }
        return null;
    }

}
