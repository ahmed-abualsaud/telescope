
package org.telescope.handler.events;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.telescope.database.IdentityManager;
import org.telescope.database.MaintenancesManager;
import org.telescope.model.Event;
import org.telescope.model.Maintenance;
import org.telescope.model.Position;

@ChannelHandler.Sharable
public class MaintenanceEventHandler extends BaseEventHandler {

    private final IdentityManager identityManager;
    private final MaintenancesManager maintenancesManager;

    public MaintenanceEventHandler(IdentityManager identityManager, MaintenancesManager maintenancesManager) {
        this.identityManager = identityManager;
        this.maintenancesManager = maintenancesManager;
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        if (identityManager.getById(position.getDeviceId()) == null
                || !identityManager.isLatestPosition(position)) {
            return null;
        }

        Position lastPosition = identityManager.getLastPosition(position.getDeviceId());
        if (lastPosition == null) {
            return null;
        }

        Map<Event, Position> events = new HashMap<>();
        for (long maintenanceId : maintenancesManager.getAllDeviceItems(position.getDeviceId())) {
            Maintenance maintenance = maintenancesManager.getById(maintenanceId);
            if (maintenance.getPeriod() != 0) {
                double oldValue = lastPosition.getDouble(maintenance.getType());
                double newValue = position.getDouble(maintenance.getType());
                if (oldValue != 0.0 && newValue != 0.0
                        && (long) ((oldValue - maintenance.getStart()) / maintenance.getPeriod())
                        < (long) ((newValue - maintenance.getStart()) / maintenance.getPeriod())) {
                    Event event = new Event(Event.TYPE_MAINTENANCE, position);
                    event.setMaintenanceId(maintenanceId);
                    event.set(maintenance.getType(), newValue);
                    events.put(event, position);
                }
            }
        }

        return events;
    }

}
