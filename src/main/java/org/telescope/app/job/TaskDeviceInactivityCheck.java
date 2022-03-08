
package org.telescope.app.job;

import org.telescope.model.Device;
import org.telescope.model.Event;
import org.telescope.model.Position;
import org.telescope.server.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskDeviceInactivityCheck implements Runnable {

    public static final String ATTRIBUTE_DEVICE_INACTIVITY_START = "deviceInactivityStart";
    public static final String ATTRIBUTE_DEVICE_INACTIVITY_PERIOD = "deviceInactivityPeriod";
    public static final String ATTRIBUTE_LAST_UPDATE = "lastUpdate";

    private static final long CHECK_PERIOD_MINUTES = 15;

    public void schedule(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate(this, CHECK_PERIOD_MINUTES, CHECK_PERIOD_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        long checkPeriod = TimeUnit.MINUTES.toMillis(CHECK_PERIOD_MINUTES);

        Map<Event, Position> events = new HashMap<>();
        for (Device device : Context.getDeviceManager().getAllDevices()) {
            if (device.getLastUpdate() != null && checkDevice(device, currentTime, checkPeriod)) {
                Event event = new Event(Event.TYPE_DEVICE_INACTIVE, device.getId());
                event.set(ATTRIBUTE_LAST_UPDATE, device.getLastUpdate().getTime());
                events.put(event, null);
            }
        }

        Context.getNotificationManager().updateEvents(events);
    }

    private boolean checkDevice(Device device, long currentTime, long checkPeriod) {
        long deviceInactivityStart = device.getLong(ATTRIBUTE_DEVICE_INACTIVITY_START);
        if (deviceInactivityStart > 0) {
            long timeThreshold = device.getLastUpdate().getTime() + deviceInactivityStart;
            if (currentTime >= timeThreshold) {

                if (currentTime - checkPeriod < timeThreshold) {
                    return true;
                }

                long deviceInactivityPeriod = device.getLong(ATTRIBUTE_DEVICE_INACTIVITY_PERIOD);
                if (deviceInactivityPeriod > 0) {
                    long count = (currentTime - timeThreshold - 1) / deviceInactivityPeriod;
                    timeThreshold += count * deviceInactivityPeriod;
                    return currentTime - checkPeriod < timeThreshold;
                }

            }
        }
        return false;
    }

}
