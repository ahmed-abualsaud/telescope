package org.telescope.server.handler;

import java.util.Map;

import org.telescope.javel.framework.storage.database.DB;

import java.util.LinkedHashMap;

public class BatteryHandler {
    
    public static void handle(
        Map<String, Object> attributes, 
        Map<String, Object> oldPosition, 
        Map<String, Object> newPosition
    ) {
        boolean charging = false;
        double power = -1, battery = -1, battery_level = -1, difference, charge_status_change_value = 100.0;
        long charge_status_changed_since = ((Number) newPosition.remove("duration_value")).longValue();
        Map<String, Object> newBattery = new LinkedHashMap<>();
        Map<String, Object> oldBattery = new LinkedHashMap<>();
        if (attributes.containsKey("power")) {
            power = ((Number) attributes.remove("power")).doubleValue();
        }
        if (attributes.containsKey("battery")) {
            battery = ((Number) attributes.remove("battery")).doubleValue();
        }
        if (attributes.containsKey("battery_level")) {
            battery_level = ((Number) attributes.remove("battery_level")).doubleValue();
        }
        if (oldPosition != null) {
            oldBattery = (Map<String, Object>) oldPosition.get("battery");
            charging = (Boolean) oldBattery.get("charging");
            charge_status_change_value = ((Number) oldBattery.get("charge_status_change_value")).doubleValue();
            charge_status_changed_since += ((Number) oldBattery.get("charge_status_changed_since")).longValue();
            for (Map.Entry<String, Object> entry : oldBattery.entrySet()) {
                if (entry.getKey().equals("charging") || 
                    entry.getKey().equals("charge_status_change_value") ||
                    entry.getKey().equals("charge_status_changed_since")) {continue;}
                difference = ((Number) entry.getValue()).doubleValue();
                if (difference != -1) {
                    difference = (entry.getKey().equals("power") ? difference - power : difference);
                    difference = (entry.getKey().equals("battery") ? difference - battery : difference);
                    difference = (entry.getKey().equals("battery_level") ? difference - battery_level : difference);
                    if (difference < 0 && !((Boolean) oldBattery.get("charging"))) {
                        charging = true;
                        Map<String, Object> history = new LinkedHashMap<>();
                        history.put("device_id", newPosition.get("device_id"));
                        history.put("unique_id", DB.table("devices").select("unique_id")
                        .find(newPosition.get("device_id")).get("unique_id"));
                        history.put("type", "BATTERY");
                        history.put("name", "Battery Start Charging");
                        history.put("description", "Battery started to charge up " +
                        charge_status_changed_since + " seconds after the last charge time" + 
                        " ,and in this period of time the charge value was dropped from " +
                        entry.getKey() +  " = " + charge_status_change_value + " to " +
                         entry.getKey() + " = " + entry.getValue());
                        DB.table("histories").create(history);
                        charge_status_changed_since = 0;
                        charge_status_change_value =  ((Number) entry.getValue()).doubleValue();
                        break;
                    }
                    if ((difference > 0 || (difference == 0 && charge_status_changed_since > 500)) &&
                       ((Boolean) oldBattery.get("charging"))) {
                        charging = false;
                        Map<String, Object> history = new LinkedHashMap<>();
                        history.put("device_id", newPosition.get("device_id"));
                        history.put("unique_id", DB.table("devices").select("unique_id")
                        .find(newPosition.get("device_id")).get("unique_id"));
                        history.put("type", "BATTERY");
                        history.put("name", "Battery End Charging");
                        history.put("description", "Battery was charged up from " +
                        entry.getKey() + " = " + charge_status_change_value + " to " +
                        entry.getKey() + " = " + entry.getValue() + " in " + 
                        charge_status_changed_since + " seconds.");
                        DB.table("histories").create(history);
                        charge_status_changed_since = 0;
                        charge_status_change_value =  ((Number) entry.getValue()).doubleValue();
                        break;
                    }
                }
            }
        }
        newBattery.put("power", power);
        newBattery.put("battery", battery);
        newBattery.put("charging", charging);
        newBattery.put("battery_level", battery_level);
        newBattery.put("charge_status_change_value", charge_status_change_value);
        newBattery.put("charge_status_changed_since", charge_status_changed_since);
        newPosition.put("battery", newBattery);
    }
}
