
package org.telescope.javel.framework.notification;

import org.apache.velocity.VelocityContext;
import org.telescope.model.Device;
import org.telescope.model.Event;
import org.telescope.model.Position;
import org.telescope.model.User;
import org.telescope.reports.ReportUtils;
import org.telescope.server.Context;

public final class NotificationFormatter {

    private NotificationFormatter() {
    }

    public static VelocityContext prepareContext(long userId, Event event, Position position) {

        User user = Context.getPermissionsManager().getUser(userId);
        Device device = Context.getIdentityManager().getById(event.getDeviceId());

        VelocityContext velocityContext = TextTemplateFormatter.prepareContext(user);

        velocityContext.put("device", device);
        velocityContext.put("event", event);
        if (position != null) {
            velocityContext.put("position", position);
            velocityContext.put("speedUnit", ReportUtils.getSpeedUnit(userId));
            velocityContext.put("distanceUnit", ReportUtils.getDistanceUnit(userId));
            velocityContext.put("volumeUnit", ReportUtils.getVolumeUnit(userId));
        }
        if (event.getGeofenceId() != 0) {
            velocityContext.put("geofence", Context.getGeofenceManager().getById(event.getGeofenceId()));
        }
        if (event.getMaintenanceId() != 0) {
            velocityContext.put("maintenance", Context.getMaintenancesManager().getById(event.getMaintenanceId()));
        }
        String driverUniqueId = event.getString(Position.KEY_DRIVER_UNIQUE_ID);
        if (driverUniqueId != null) {
            velocityContext.put("driver", Context.getDriversManager().getDriverByUniqueId(driverUniqueId));
        }

        return velocityContext;
    }

    public static FullMessage formatFullMessage(long userId, Event event, Position position) {
        VelocityContext velocityContext = prepareContext(userId, event, position);
        return TextTemplateFormatter.formatFullMessage(velocityContext, event.getType());
    }

    public static String formatShortMessage(long userId, Event event, Position position) {
        VelocityContext velocityContext = prepareContext(userId, event, position);
        return TextTemplateFormatter.formatShortMessage(velocityContext, event.getType());
    }

}
