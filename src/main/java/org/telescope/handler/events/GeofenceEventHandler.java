
package org.telescope.handler.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.telescope.database.CalendarManager;
import org.telescope.database.GeofenceManager;
import org.telescope.database.IdentityManager;
import org.telescope.model.Calendar;
import org.telescope.model.Device;
import org.telescope.model.Event;
import org.telescope.model.Position;

@ChannelHandler.Sharable
public class GeofenceEventHandler extends BaseEventHandler {

    private final IdentityManager identityManager;
    private final GeofenceManager geofenceManager;
    private final CalendarManager calendarManager;

    public GeofenceEventHandler(
            IdentityManager identityManager, GeofenceManager geofenceManager, CalendarManager calendarManager) {
        this.identityManager = identityManager;
        this.geofenceManager = geofenceManager;
        this.calendarManager = calendarManager;
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        Device device = identityManager.getById(position.getDeviceId());
        if (device == null) {
            return null;
        }
        if (!identityManager.isLatestPosition(position) || !position.getValid()) {
            return null;
        }

        List<Long> currentGeofences = geofenceManager.getCurrentDeviceGeofences(position);
        List<Long> oldGeofences = new ArrayList<>();
        if (device.getGeofenceIds() != null) {
            oldGeofences.addAll(device.getGeofenceIds());
        }
        List<Long> newGeofences = new ArrayList<>(currentGeofences);
        newGeofences.removeAll(oldGeofences);
        oldGeofences.removeAll(currentGeofences);

        device.setGeofenceIds(currentGeofences);

        Map<Event, Position> events = new HashMap<>();
        for (long geofenceId : oldGeofences) {
            long calendarId = geofenceManager.getById(geofenceId).getCalendarId();
            Calendar calendar = calendarId != 0 ? calendarManager.getById(calendarId) : null;
            if (calendar == null || calendar.checkMoment(position.getFixTime())) {
                Event event = new Event(Event.TYPE_GEOFENCE_EXIT, position);
                event.setGeofenceId(geofenceId);
                events.put(event, position);
            }
        }
        for (long geofenceId : newGeofences) {
            long calendarId = geofenceManager.getById(geofenceId).getCalendarId();
            Calendar calendar = calendarId != 0 ? calendarManager.getById(calendarId) : null;
            if (calendar == null || calendar.checkMoment(position.getFixTime())) {
                Event event = new Event(Event.TYPE_GEOFENCE_ENTER, position);
                event.setGeofenceId(geofenceId);
                events.put(event, position);
            }
        }
        return events;
    }

}
