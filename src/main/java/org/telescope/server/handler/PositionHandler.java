package org.telescope.server.handler;

import java.util.Map;

public class PositionHandler {

    public static void handle(
        Map<String, Object> state, 
        Map<String, Object> attributes,
        Map<String, Object> oldPosition,
        Map<String, Object> newPosition
    ) {
        long count = 0;
        if (oldPosition != null) {
            count = ((Number) oldPosition.get("count")).longValue();
        }
        newPosition.put("attributes", attributes);
        newPosition.put("valid", state.get("valid"));
        newPosition.put("course", state.get("course"));
        newPosition.put("network", state.get("network"));
        newPosition.put("protocol", state.get("protocol"));
        newPosition.put("accuracy", state.get("accuracy"));
        newPosition.put("altitude", state.get("altitude"));
        newPosition.put("latitude", state.get("latitude"));
        newPosition.put("device_id", state.get("deviceId"));
        newPosition.put("longitude", state.get("longitude"));
        newPosition.put("count", (count + 1));
        newPosition.put("fix_time", state.get("fixTime").toString().replace('T', ' ').substring(0, 19));
        newPosition.put("device_time", state.get("deviceTime").toString().replace('T', ' ').substring(0, 19));
    }
}
