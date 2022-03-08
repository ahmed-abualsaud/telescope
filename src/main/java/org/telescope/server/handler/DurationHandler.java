package org.telescope.server.handler;

import java.util.Map;

import org.telescope.javel.framework.helper.DateUtil;

import java.util.LinkedHashMap;

public class DurationHandler {
    
    public static void handle(
        Map<String, Object> state, 
        Map<String, Object> oldPosition, 
        Map<String, Object> newPosition
    ) {
        long duration = 0, duration_min = 3600L,
        duration_max = 0, duration_avg = 0, duration_sum = 0;
        Map<String, Object> oldDuration = new LinkedHashMap<>();
        Map<String, Object> newDuration = new LinkedHashMap<>();
        if (oldPosition != null) {
            oldDuration = (Map<String, Object>) oldPosition.get("duration");
            duration = DateUtil.parseDateMillis(state.get("fixTime").toString());
            duration -= DateUtil.parseDateMillis(oldPosition.get("fix_time").toString());
            duration /= 1000;
            duration_min = ((Number) oldDuration.get("min")).longValue();
            duration_min = ((duration < duration_min) ? duration : duration_min);
            duration_max = ((Number) oldDuration.get("max")).longValue();
            duration_max = ((duration > duration_max) ? duration : duration_max);
            duration_sum = duration + ((Number) oldDuration.get("sum")).longValue();
            duration_avg = duration_sum / ((Number) newPosition.get("count")).longValue();
        }
        newDuration.put("val", duration);
        newDuration.put("min", duration_min);
        newDuration.put("max", duration_max);
        newDuration.put("avg", duration_avg);
        newDuration.put("sum", duration_sum);
        newPosition.put("duration", newDuration);
        newPosition.put("duration_avg", duration_avg);
        newPosition.put("duration_value", (duration == 0 ? 1 : duration));
    }
    
}
