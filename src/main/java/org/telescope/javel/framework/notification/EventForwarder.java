
package org.telescope.javel.framework.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.config.Keys;
import org.telescope.model.Device;
import org.telescope.model.Event;
import org.telescope.model.Geofence;
import org.telescope.model.Maintenance;
import org.telescope.model.Position;
import org.telescope.server.Context;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EventForwarder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventForwarder.class);

    private final String url;
    private final String header;

    public EventForwarder() {
        url = Context.getConfig().getString(Keys.EVENT_FORWARD_URL);
        header = Context.getConfig().getString(Keys.EVENT_FORWARD_HEADERS);
    }

    private static final String KEY_POSITION = "position";
    private static final String KEY_EVENT = "event";
    private static final String KEY_GEOFENCE = "geofence";
    private static final String KEY_DEVICE = "device";
    private static final String KEY_MAINTENANCE = "maintenance";
    private static final String KEY_USERS = "users";

    public final void forwardEvent(Event event, Position position, Set<Long> users) {

        Invocation.Builder requestBuilder = Context.getClient().target(url).request();

        if (header != null && !header.isEmpty()) {
            for (String line: header.split("\\r?\\n")) {
                String[] values = line.split(":", 2);
                requestBuilder.header(values[0].trim(), values[1].trim());
            }
        }

        LOGGER.debug("Event forwarding initiated");
        requestBuilder.async().post(
                Entity.json(preparePayload(event, position, users)), new InvocationCallback<Object>() {
                    @Override
                    public void completed(Object o) {
                        LOGGER.debug("Event forwarding succeeded");
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        LOGGER.warn("Event forwarding failed", throwable);
                    }
                });
    }

    protected Map<String, Object> preparePayload(Event event, Position position, Set<Long> users) {
        Map<String, Object> data = new HashMap<>();
        data.put(KEY_EVENT, event);
        if (position != null) {
            data.put(KEY_POSITION, position);
        }
        Device device = Context.getIdentityManager().getById(event.getDeviceId());
        if (device != null) {
            data.put(KEY_DEVICE, device);
        }
        if (event.getGeofenceId() != 0) {
            Geofence geofence = Context.getGeofenceManager().getById(event.getGeofenceId());
            if (geofence != null) {
                data.put(KEY_GEOFENCE, geofence);
            }
        }
        if (event.getMaintenanceId() != 0) {
            Maintenance maintenance = Context.getMaintenancesManager().getById(event.getMaintenanceId());
            if (maintenance != null) {
                data.put(KEY_MAINTENANCE, maintenance);
            }
        }
        data.put(KEY_USERS, Context.getUsersManager().getItems(users));
        return data;
    }

}
