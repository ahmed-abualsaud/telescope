package org.telescope.server.handler;

import java.util.Map;

import org.telescope.javel.framework.helper.UnitsConverter;

import java.util.LinkedHashMap;

public class SpeedHandler {

    public static void handle(
        Map<String, Object> state, 
        Map<String, Object> oldPosition,
        Map<String, Object> newPosition
    ) {
        double speed = UnitsConverter.metersPerSecondFromKnots(((Number) state.get("speed")).doubleValue()), 
        speed_max = 0.0, speed_min = 300000000D, speed_avg = 0.0, speed_sum = 0.0;
        Map<String, Object> oldSpeed = new LinkedHashMap<>();
        Map<String, Object> newSpeed = new LinkedHashMap<>();
        if (oldPosition != null) {
            oldSpeed = (Map<String, Object>) oldPosition.get("speed");
            speed_min = ((Number) oldSpeed.get("min")).doubleValue();
            speed_min = ((speed < speed_min) ? speed : speed_min);
            speed_max = ((Number) oldSpeed.get("max")).doubleValue();
            speed_max = ((speed > speed_max) ? speed : speed_max);
            speed_sum = speed + ((Number) oldSpeed.get("sum")).doubleValue();
            speed_avg = speed_sum / ((Number) newPosition.get("count")).longValue();
        }
        newSpeed.put("val", speed);
        newSpeed.put("min", speed_min);
        newSpeed.put("max", speed_max);
        newSpeed.put("avg", speed_avg);
        newSpeed.put("sum", speed_sum);
        newPosition.put("speed", newSpeed);
    }
}
