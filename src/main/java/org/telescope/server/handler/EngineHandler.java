package org.telescope.server.handler;

import java.util.Map;

import org.telescope.javel.framework.storage.database.DB;

import java.util.LinkedHashMap;

public class EngineHandler {

    public static void handle(
        Map<String, Object> attributes,
        Map<String, Object> oldPosition, 
        Map<String, Object> newPosition
    ) {
        long engine_status_changed_since = 0, work_duration = 0,
        duration_value = ((Number) newPosition.get("duration_value")).longValue();
        String status = null;
        Map<String, Object> oldEngine = new LinkedHashMap<>();
        Map<String, Object> newEngine = new LinkedHashMap<>();
        if (attributes.containsKey("ignition")) {
            status = ((Boolean) attributes.get("ignition") ? "ON" : "OFF");
            Map<String, Object> history = new LinkedHashMap<>();
            history.put("device_id", newPosition.get("device_id"));
            history.put("unique_id", DB.table("devices").select("unique_id")
            .find(newPosition.get("device_id")).get("unique_id"));
            history.put("type", "IGNITION");
            if ((Boolean) attributes.get("ignition")) {
                history.put("name", "Engine Start Running");
                history.put("description", "Engine started to run " +
                engine_status_changed_since + " seconds after the last working time.");
            } else {
                history.put("name", "Engine Stop Running");
                history.put("description", "Engine stopped working after " +
                engine_status_changed_since + " seconds of running.");
            }
            
            DB.table("histories").create(history);
            engine_status_changed_since = 0;
        }
        if (oldPosition != null) {
            oldEngine = (Map<String, Object>) oldPosition.get("engine");
            work_duration = ((Number) oldEngine.get("work_duration")).longValue();
            if (!attributes.containsKey("ignition")) {
                if (oldEngine.get("status") != null) 
                {status = oldEngine.get("status").toString();}
                engine_status_changed_since = duration_value + ((Number) 
                oldEngine.get("engine_status_changed_since")).longValue();
            }
        }
        work_duration += ((status != null && status.equals("ON"))? duration_value : 0);
        attributes.remove("ignition");
        newEngine.put("status", status);
        newEngine.put("work_duration", work_duration);
        newEngine.put("engine_status_changed_since", engine_status_changed_since);


        double fuel_level = 0.0, fuel_used_value = 0.0, fuel_used_sum = 0.0,
        fuel_used_avg = 0.0, fuel_drop_value = 0.0, fuel_drop_rate = 0.0,
        fuel_drop_rate_min = 999999999999D, fuel_drop_rate_max = 0.0,
        fuel_drop_avg = 0.0, fuel_drop_sum = 0.0;
        long fuel_run_out_after = 0, fuel_last_filling_since = 0, fuel_filling_count = 1,
        duration_avg = ((Number) newPosition.remove("duration_avg")).longValue();
        boolean fuel_dropped = false;
        if (attributes.containsKey("fuel_consumption")) {
            fuel_drop_value = ((Number) attributes.get("fuel_consumption")).doubleValue();
            fuel_drop_value *= duration_value / 3600;
            fuel_dropped = true;
        }
        if (attributes.containsKey("fuel")) {
            fuel_level = ((Number) attributes.get("fuel")).doubleValue();
        }
        if (attributes.containsKey("fuel_used")) {
            fuel_used_value = ((Number) attributes.get("fuel_used")).doubleValue();
        }
        if (oldPosition != null) {
            if (!attributes.containsKey("fuel_consumption") && attributes.containsKey("fuel")) {
                fuel_drop_value = ((Number) oldEngine.get("fuel_level")).doubleValue() - fuel_level;
                fuel_dropped = true;
            }
            if (!attributes.containsKey("fuel_consumption") && !attributes.containsKey("fuel") &&
                 attributes.containsKey("fuel_used")) {
                fuel_drop_value = fuel_used_value - ((Number) oldEngine.get("fuel_used_value")).doubleValue();
                fuel_dropped = true;
            }
            if (fuel_dropped) {
                if (fuel_drop_value < 0.0) {
                    fuel_drop_value = ((Number) oldEngine.get("fuel_drop_avg")).doubleValue();
                    Map<String, Object> history = new LinkedHashMap<>();
                    history.put("device_id", newPosition.get("device_id"));
                    history.put("unique_id", DB.table("devices").select("unique_id")
                    .find(newPosition.get("device_id")).get("unique_id"));
                    history.put("type", "FUEL");
                    history.put("name", "Refueling Started");
                    history.put("description", "Refueling started " +
                    fuel_last_filling_since + " seconds after the last refueling time.");
                    DB.table("histories").create(history);
                    fuel_last_filling_since = duration_avg;
                } else {fuel_last_filling_since += duration_value;}
            }
            if (attributes.containsKey("fuel")) {
                if (fuel_level > ((Number) oldEngine.get("fuel_level")).doubleValue()) {
                    fuel_used_sum = ((Number) oldEngine.get("fuel_used_sum")).doubleValue();
                    fuel_used_sum += fuel_used_value;
                    fuel_used_value = fuel_drop_value;
                    fuel_filling_count++;
                    fuel_used_avg = fuel_used_sum / fuel_filling_count;
                } 
                else {fuel_used_value = ((Number) oldEngine.get("fuel_used_value")).doubleValue() + fuel_drop_value;}
            }
            if (!attributes.containsKey("fuel") && attributes.containsKey("fuel_used")) {
                if (fuel_used_value < ((Number) oldEngine.get("fuel_used_value")).doubleValue()) {
                    fuel_used_sum = ((Number) oldEngine.get("fuel_used_sum")).doubleValue();
                    fuel_used_sum += ((Number) oldEngine.get("fuel_used_value")).doubleValue() + fuel_drop_value;
                    fuel_filling_count++;
                    fuel_used_avg = fuel_used_sum / fuel_filling_count;
                    fuel_level = fuel_used_avg;
                }
                else {if (fuel_level > 0.0) {fuel_level -= fuel_drop_value;}}
            }
            fuel_drop_sum = fuel_drop_value + ((Number) oldEngine.get("fuel_drop_sum")).doubleValue();
            fuel_drop_avg = fuel_drop_sum / ((Number) newPosition.get("count")).longValue();
            fuel_drop_rate = (duration_avg != 0.0 ? (fuel_drop_avg / duration_avg) : fuel_drop_avg);
            fuel_drop_rate_min = ((Number) oldEngine.get("fuel_drop_rate_min")).doubleValue();
            fuel_drop_rate_min = ((fuel_drop_rate < fuel_drop_rate_min) ? fuel_drop_rate : fuel_drop_rate_min);
            fuel_drop_rate_max = ((Number) oldEngine.get("fuel_drop_rate_max")).doubleValue();
            fuel_drop_rate_max = ((fuel_drop_rate > fuel_drop_rate_max) ? fuel_drop_rate : fuel_drop_rate_max);
            fuel_run_out_after = (long) ((fuel_drop_rate != 0.0 )? (fuel_level / fuel_drop_rate) : fuel_level);
            fuel_level = (fuel_level < 0.0 ? 0.0 : fuel_level);
        }
        if (!attributes.containsKey("fuel") && !attributes.containsKey("fuel_used")) {
            fuel_level = 0.0;
            fuel_used_value = 0.0;
            fuel_filling_count = 0;
        }
        attributes.remove("fuel");
        attributes.remove("fuel_used");
        attributes.remove("fuel_consumption");
        newEngine.put("fuel_level", fuel_level);
        newEngine.put("fuel_used_value", fuel_used_value);
        newEngine.put("fuel_used_avg", fuel_used_avg);
        newEngine.put("fuel_used_sum", fuel_used_sum);
        newEngine.put("fuel_drop_value", fuel_drop_value);
        newEngine.put("fuel_drop_rate", fuel_drop_rate);
        newEngine.put("fuel_drop_rate_min", fuel_drop_rate_min);
        newEngine.put("fuel_drop_rate_max", fuel_drop_rate_max);
        newEngine.put("fuel_drop_avg", fuel_drop_avg);
        newEngine.put("fuel_drop_sum", fuel_drop_sum);
        newEngine.put("fuel_filling_count", fuel_filling_count);
        newEngine.put("fuel_run_out_after", fuel_run_out_after);
        newEngine.put("fuel_last_filling_since", fuel_last_filling_since);
        newPosition.put("engine", newEngine);
    }
}
