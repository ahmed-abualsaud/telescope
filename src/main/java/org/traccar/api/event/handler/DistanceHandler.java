package org.traccar.api.event.handler;

import org.traccar.helper.DistanceCalculator;

import java.util.Map;

public class DistanceHandler {

    public static void handle(
        Map<String, Object> state, 
        Map<String, Object> attributes,
        Map<String, Object> oldPosition, 
        Map<String, Object> newPosition
    ) {
        double distance = 0.0;
        double total_distance = 0.0;
        if (attributes.containsKey("distance")) {
            distance = ((Number) attributes.get("distance")).doubleValue();
        }
        if (oldPosition != null) {
            total_distance = ((Number) oldPosition.get("total_distance")).doubleValue();
            if (!attributes.containsKey("distance")) {
                distance = DistanceCalculator.distance(
                    ((Number) state.get("latitude")).doubleValue(),
                    ((Number) state.get("longitude")).doubleValue(),
                    ((Number) oldPosition.get("latitude")).doubleValue(),
                    ((Number) oldPosition.get("longitude")).doubleValue());
            }
        }
        newPosition.put("distance", distance);
        newPosition.put("total_distance", (total_distance + distance));
    }
    
}
