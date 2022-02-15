package org.traccar.api.event.handler;

import java.util.Map;

public class AccelerationHandler {

    public static void handle(
        Map<String, Object> state, 
        Map<String, Object> attributes,
        Map<String, Object> oldPosition,
        Map<String, Object> newPosition
    ) {
        double acceleration = 0.0;
        if (attributes.containsKey("acceleration")) {
            acceleration = ((Number) attributes.get("acceleration")).doubleValue();
        }
        if (oldPosition != null && !attributes.containsKey("acceleration")) {
            acceleration = ((Number) state.get("speed")).doubleValue();
            acceleration -= ((Number) oldPosition.get("speed")).doubleValue();
            acceleration /= ((Number) newPosition.get("duration")).doubleValue();
        }
        newPosition.put("acceleration", acceleration);
    }
    
}
