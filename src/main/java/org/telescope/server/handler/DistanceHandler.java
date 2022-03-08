package org.telescope.server.handler;


import java.util.Map;

import org.telescope.javel.framework.helper.DistanceCalculator;

import java.util.LinkedHashMap;

public class DistanceHandler {

    public static void handle(
        Map<String, Object> state, 
        Map<String, Object> attributes,
        Map<String, Object> oldPosition, 
        Map<String, Object> newPosition
    ) {
        double distance = 0.0, distance_min = 30000000000D,
        distance_max = 0.0, distance_avg = 0.0, distance_sum = 0.0;
        Map<String, Object> oldDistance = new LinkedHashMap<>();
        Map<String, Object> newDistance = new LinkedHashMap<>();
        if (attributes.containsKey("distance")) {
            distance = ((Number) attributes.get("distance")).doubleValue();
        }
        if (oldPosition != null) {
            if (!attributes.containsKey("distance")) {
                distance = DistanceCalculator.distance(
                    ((Number) state.get("latitude")).doubleValue(),
                    ((Number) state.get("longitude")).doubleValue(),
                    ((Number) oldPosition.get("latitude")).doubleValue(),
                    ((Number) oldPosition.get("longitude")).doubleValue());
            }
            oldDistance = (Map<String, Object>) oldPosition.get("distance");
            distance_min = ((Number) oldDistance.get("min")).doubleValue();
            distance_min = ((distance < distance_min) ? distance : distance_min);
            distance_max = ((Number) oldDistance.get("max")).doubleValue();
            distance_max = ((distance > distance_max) ? distance : distance_max);
            distance_sum = distance + ((Number) oldDistance.get("sum")).doubleValue();
            distance_avg = distance_sum / ((Number) newPosition.get("count")).longValue();
        }
        attributes.remove("distance");
        newDistance.put("val", distance);
        newDistance.put("min", distance_min);
        newDistance.put("max", distance_max);
        newDistance.put("avg", distance_avg);
        newDistance.put("sum", distance_sum);
        newPosition.put("distance", newDistance);
    }
    
}
