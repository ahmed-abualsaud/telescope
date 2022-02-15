package org.traccar.api.event.handler;

import org.traccar.Context;
import org.traccar.config.Keys;

import java.util.Map;

public class MotionHandler {

    public static void handle(
        Map<String, Object> state, 
        Map<String, Object> attributes,
        Map<String, Object> newPosition
    ) {
        if (!attributes.containsKey("motion")) {
            double threshold = Context.getConfig().getDouble(Keys.MOTION_SPEED_THRESHOLD);
            newPosition.put("motion", ((Number) state.get("speed")).doubleValue() > threshold);
        }
    }
    
}
