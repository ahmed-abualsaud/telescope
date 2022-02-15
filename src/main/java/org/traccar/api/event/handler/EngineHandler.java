package org.traccar.api.event.handler;

import org.traccar.helper.JSON;

import java.util.Map;
import java.util.LinkedHashMap;

public class EngineHandler {

    public static void handle(
        Map<String, Object> attributes,
        Map<String, Object> oldPosition, 
        Map<String, Object> newPosition
    ) {
        long since = 0;
        String status = null;
        Map<String, Object> oldEngine = new LinkedHashMap<>();
        Map<String, Object> newEngine = new LinkedHashMap<>();
        if (oldPosition != null) {
            oldEngine = JSON.decode(oldPosition.get("engine").toString());
        }
        if (attributes.containsKey("ignition")) {
            if ((Boolean) attributes.get("ignition")) {status = "ON";}
            else {status = "OFF";}
            since = 0;
        }
        if (oldPosition != null && !attributes.containsKey("ignition")) {
            if (oldEngine.get("status") != null) 
            {status = oldEngine.get("status").toString();}
            since = ((Number) oldEngine.get("since")).longValue();
            since += ((Number) newPosition.get("duration")).longValue();
        }
        attributes.remove("ignition");
        newEngine.put("status", status);
        newEngine.put("since", since);
        
        double fuelLevel = 0;
        double fuelDrop = 0;
        if (attributes.containsKey("fuel")) {
            fuelLevel = ((Number) attributes.get("fuel")).doubleValue();
        } 
        if (oldPosition != null && !attributes.containsKey("fuel")) {
            fuelLevel = ((Number) oldEngine.get("fuel_level")).doubleValue();
            fuelDrop = ((Number) oldEngine.get("fuel_drop")).doubleValue();
        }
        if (oldPosition != null && attributes.containsKey("fuel")) {
            fuelDrop = fuelLevel - ((Number) oldEngine.get("fuel_level")).doubleValue();
        }
        attributes.remove("fuel");
        newEngine.put("fuel_level", fuelLevel);
        newEngine.put("fuel_drop", fuelDrop);
        newPosition.put("engine", newEngine);
    }
    
}
