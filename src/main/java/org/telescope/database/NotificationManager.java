package org.telescope.database;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.config.Keys;
import org.telescope.model.Calendar;
import org.telescope.model.Event;
import org.telescope.model.Notification;
import org.telescope.model.Position;
import org.telescope.model.Typed;
import org.telescope.server.Context;

public class NotificationManager extends ExtendedObjectManager<Notification> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationManager.class);

    private final boolean geocodeOnRequest;

    public NotificationManager(DataManager dataManager) {
        super(dataManager, Notification.class);
        geocodeOnRequest = Context.getConfig().getBoolean(Keys.GEOCODER_ON_REQUEST);
    }

    private Set<Long> getEffectiveNotifications(long userId, long deviceId, Date time) {
        Set<Long> result = new HashSet<>();
        Set<Long> deviceNotifications = getAllDeviceItems(deviceId);
        for (long itemId : getUserItems(userId)) {
            if (getById(itemId).getAlways() || deviceNotifications.contains(itemId)) {
                long calendarId = getById(itemId).getCalendarId();
                Calendar calendar = calendarId != 0 ? Context.getCalendarManager().getById(calendarId) : null;
                if (calendar == null || calendar.checkMoment(time)) {
                    result.add(itemId);
                }
            }
        }
        return result;
    }

    public void updateEvent(Event event, Position position) {
        try {
            getDataManager().addObject(event);
        } catch (SQLException error) {
            LOGGER.warn("Event save error", error);
        }

        long deviceId = event.getDeviceId();
        Set<Long> users = Context.getPermissionsManager().getDeviceUsers(deviceId);
        Set<Long> usersToForward = null;
        if (Context.getEventForwarder() != null) {
            usersToForward = new HashSet<>();
        }
        for (long userId : users) {
            if ((event.getGeofenceId() == 0
                    || Context.getGeofenceManager().checkItemPermission(userId, event.getGeofenceId()))
                    && (event.getMaintenanceId() == 0
                    || Context.getMaintenancesManager().checkItemPermission(userId, event.getMaintenanceId()))) {
                if (usersToForward != null) {
                    usersToForward.add(userId);
                }
                final Set<String> notificators = new HashSet<>();
                for (long notificationId : getEffectiveNotifications(userId, deviceId, event.getEventTime())) {
                    Notification notification = getById(notificationId);
                    if (getById(notificationId).getType().equals(event.getType())) {
                        boolean filter = false;
                        if (event.getType().equals(Event.TYPE_ALARM)) {
                            String alarmsAttribute = notification.getString("alarms");
                            if (alarmsAttribute == null) {
                                filter = true;
                            } else {
                                List<String> alarms = Arrays.asList(alarmsAttribute.split(","));
                                filter = !alarms.contains(event.getString(Position.KEY_ALARM));
                            }
                        }
                        if (!filter) {
                            notificators.addAll(notification.getNotificatorsTypes());
                        }
                    }
                }

                if (position != null && position.getAddress() == null
                        && geocodeOnRequest && Context.getGeocoder() != null) {
                    position.setAddress(Context.getGeocoder()
                            .getAddress(position.getLatitude(), position.getLongitude(), null));
                }

                for (String notificator : notificators) {
                    Context.getNotificatorManager().getNotificator(notificator).sendAsync(userId, event, position);
                }
            }
        }
        if (Context.getEventForwarder() != null) {
            Context.getEventForwarder().forwardEvent(event, position, usersToForward);
        }
    }

    public void updateEvents(Map<Event, Position> events) {
        for (Entry<Event, Position> event : events.entrySet()) {
            updateEvent(event.getKey(), event.getValue());
        }
    }

    public Set<Typed> getAllNotificationTypes() {
        Set<Typed> types = new HashSet<>();
        Field[] fields = Event.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && field.getName().startsWith("TYPE_")) {
                try {
                    types.add(new Typed(field.get(null).toString()));
                } catch (IllegalArgumentException | IllegalAccessException error) {
                    LOGGER.warn("Get event types error", error);
                }
            }
        }
        return types;
    }
}
