package org.telescope.server.handler;

import org.telescope.config.Keys;
import org.telescope.javel.framework.storage.database.DB;
import org.telescope.server.Context;

import java.util.Map;
import java.util.LinkedHashMap;

public class MotionHandler {

    public static void handle(
        Map<String, Object> state, 
        Map<String, Object> attributes,
        Map<String, Object> oldPosition,
        Map<String, Object> newPosition
    ) {
        boolean motion;
        long motion_status_changed_since = ((Number) newPosition.get("duration_value")).longValue();
        Map<String, Object> newMotion = new LinkedHashMap<>();
        Map<String, Object> oldMotion = new LinkedHashMap<>();
        if (!attributes.containsKey("motion")) {
            double threshold = Context.getConfig().getDouble(Keys.MOTION_SPEED_THRESHOLD);
            motion = (((Number) state.get("speed")).doubleValue() > threshold);
        } else {
            motion = (Boolean) attributes.remove("motion");
        }
        if (oldPosition != null) {
            oldMotion = (Map<String, Object>) oldPosition.get("motion");
            motion_status_changed_since += ((Number) oldMotion.get("motion_status_changed_since")).longValue();
            if (motion && !((Boolean) oldMotion.get("val"))) {
                Map<String, Object> history = new LinkedHashMap<>();
                history.put("device_id", newPosition.get("device_id"));
                history.put("unique_id", DB.table("devices").select("unique_id")
                .find(newPosition.get("device_id")).get("unique_id"));
                history.put("type", "MOTION");
                history.put("name", "Vehicle Stop Moving");
                history.put("description", "Vehicle stopped moving after " +
                motion_status_changed_since + " seconds in motion.");
                DB.table("histories").create(history);
                motion_status_changed_since = 0;
            }
            if (!motion && ((Boolean) oldMotion.get("val"))) {
                Map<String, Object> history = new LinkedHashMap<>();
                history.put("device_id", newPosition.get("device_id"));
                history.put("unique_id", DB.table("devices").select("unique_id")
                .find(newPosition.get("device_id")).get("unique_id"));
                history.put("type", "MOTION");
                history.put("name", "Vehicle Start Moving");
                history.put("description", "Vehicle started to move " +
                motion_status_changed_since + " seconds after the last moving time.");
                DB.table("histories").create(history);
                motion_status_changed_since = 0;
            }
        }
        newMotion.put("val", motion);
        newMotion.put("motion_status_changed_since", motion_status_changed_since);
        newPosition.put("motion", newMotion);
    }
    
}
