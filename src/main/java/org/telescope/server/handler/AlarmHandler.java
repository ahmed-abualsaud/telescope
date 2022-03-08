package org.telescope.server.handler;

import java.util.Map;

import org.telescope.javel.framework.storage.database.DB;

import java.util.LinkedHashMap;

public class AlarmHandler {
    
    public static void handle(
        Map<String, Object> attributes, 
        Map<String, Object> newPosition
    ) {
        String alarm = null;
        if (attributes.containsKey("alarm")) {
            alarm = attributes.remove("alarm").toString();
            Map<String, Object> history = new LinkedHashMap<>();
            history.put("device_id", newPosition.get("device_id"));
            history.put("unique_id", DB.table("devices").select("unique_id")
            .find(newPosition.get("device_id")).get("unique_id"));
            history.put("type", "ALARM");
            history.put("name", "Alarm Triggerd");
            history.put("description", alarm + " alarm triggered.");
        }
        newPosition.put("alarm", alarm);
    }
}
