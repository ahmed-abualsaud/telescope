package org.traccar.api.event.handler;

import org.traccar.helper.DateUtil;

import java.util.Map;

public class DurationHandler {
    
    public static void handle(
        Map<String, Object> state, 
        Map<String, Object> oldPosition, 
        Map<String, Object> newPosition
    ) {
        long duration = 0;
        long total_duration = 0;
        if (oldPosition != null) {
            total_duration = ((Number) oldPosition.get("total_duration")).longValue();
            duration = DateUtil.parseDateMillis(state.get("fixTime").toString());
            duration -= DateUtil.parseDateMillis(oldPosition.get("fix_time").toString());
            duration /= 1000;
            total_duration += duration;
        }
        newPosition.put("duration", duration);
        newPosition.put("total_duration", total_duration);
    }
    
}
