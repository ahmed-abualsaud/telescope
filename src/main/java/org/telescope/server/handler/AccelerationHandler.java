package org.telescope.server.handler;

import java.util.Map;
import java.util.LinkedHashMap;

public class AccelerationHandler {

    public static void handle(
        Map<String, Object> state, 
        Map<String, Object> attributes,
        Map<String, Object> oldPosition,
        Map<String, Object> newPosition
    ) {
        double acceleration = 0.0, 
        positive_max = 0.0, positive_min = 300000000D,
        positive_avg = 0.0, positive_sum = 0.0,
        negative_min = 0.0, negative_max = -300000000D,
        negative_avg = 0.0, negative_sum = 0.0,
        total_max = 0.0, total_min = 0.0,
        total_avg = 0.0, total_sum = 0.0;
        Map<String, Object> oldAcceleration = new LinkedHashMap<>();
        Map<String, Object> newAcceleration = new LinkedHashMap<>();
        if (attributes.containsKey("acceleration")) {
            acceleration = ((Number) attributes.get("acceleration")).doubleValue();
        }
        if (oldPosition != null) {
            if (!attributes.containsKey("acceleration")) {
                acceleration = ((Number) state.get("speed")).doubleValue();
                acceleration -= ((Number) ((Map<String, Object>) oldPosition.get("speed")).get("val")).doubleValue();
                acceleration /= ((Number) newPosition.get("duration_value")).doubleValue();
            }
            oldAcceleration = (Map<String, Object>) oldPosition.get("acceleration");
            total_min = ((Number) oldAcceleration.get("min")).doubleValue();
            total_min = ((acceleration < total_min) ? acceleration : total_min);
            total_max = ((Number) oldAcceleration.get("max")).doubleValue();
            total_max = ((acceleration > total_max) ? acceleration : total_max);
            total_sum = ((Number) oldAcceleration.get("sum")).doubleValue();
            total_sum += acceleration;
            total_avg = total_sum / ((Number) newPosition.get("count")).longValue();
            
            if (acceleration >= 0.0) {
                positive_min = ((Number) oldAcceleration.get("positive_min")).doubleValue();
                positive_min = ((acceleration < positive_min) ? acceleration : positive_min);
                positive_max = ((Number) oldAcceleration.get("positive_max")).doubleValue();
                positive_max = ((acceleration > positive_max) ? acceleration : positive_max);
                positive_sum = ((Number) oldAcceleration.get("positive_sum")).doubleValue();
                positive_sum += acceleration;
                positive_avg = positive_sum / ((Number) newPosition.get("count")).longValue();
                negative_min = ((Number) oldAcceleration.get("negative_min")).doubleValue();
                negative_max = ((Number) oldAcceleration.get("negative_max")).doubleValue();
                negative_avg = ((Number) oldAcceleration.get("negative_avg")).doubleValue();
                negative_sum = ((Number) oldAcceleration.get("negative_sum")).doubleValue();
            } else {
                negative_min = ((Number) oldAcceleration.get("negative_min")).doubleValue();
                negative_min = ((acceleration < negative_min) ? acceleration : negative_min);
                negative_max = ((Number) oldAcceleration.get("negative_max")).doubleValue();
                negative_max = ((acceleration > negative_max) ? acceleration : negative_max);
                negative_sum = ((Number) oldAcceleration.get("negative_sum")).doubleValue();
                negative_sum += acceleration;
                negative_avg = negative_sum / ((Number) newPosition.get("count")).longValue();
                positive_min = ((Number) oldAcceleration.get("positive_min")).doubleValue();
                positive_max = ((Number) oldAcceleration.get("positive_max")).doubleValue();
                positive_avg = ((Number) oldAcceleration.get("positive_avg")).doubleValue();
                positive_sum = ((Number) oldAcceleration.get("positive_sum")).doubleValue();
            }
        }
        attributes.remove("acceleration");
        newAcceleration.put("val", acceleration);
        newAcceleration.put("min", total_min);
        newAcceleration.put("max", total_max);
        newAcceleration.put("avg", total_avg);
        newAcceleration.put("sum", total_sum);
        newAcceleration.put("positive_min", positive_min);
        newAcceleration.put("positive_max", positive_max);
        newAcceleration.put("positive_avg", positive_avg);
        newAcceleration.put("positive_sum", positive_sum);
        newAcceleration.put("negative_min", negative_min);
        newAcceleration.put("negative_max", negative_max);
        newAcceleration.put("negative_avg", negative_avg);
        newAcceleration.put("negative_sum", negative_sum);
        newPosition.put("acceleration", newAcceleration);
    }
    
}
