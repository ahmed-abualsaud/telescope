package org.traccar.api.event;

import org.traccar.helper.JSON;
import org.traccar.database.DB;

import org.traccar.api.event.handler.EngineHandler;
import org.traccar.api.event.handler.MotionHandler;
import org.traccar.api.event.handler.DistanceHandler;
import org.traccar.api.event.handler.DurationHandler;
import org.traccar.api.event.handler.PositionHandler;
import org.traccar.api.event.handler.AccelerationHandler;

import java.util.Map;
import java.util.HashMap;

public class DeviceUpdateState extends Event {

    private String channel;
    private Map<String, Object> data = new HashMap<>();
    
    public DeviceUpdateState(Object message) {

        Map<String, Object> newPosition = new HashMap<>();
        Map<String, Object> state = JSON.decode(message);
        Map<String, Object> attributes = JSON.decode(state.get("attributes"));
        Map<String, Object> oldPosition = DB.table("positions")
            .select("latitude", "longitude", "device_id", "user_id", "fix_time", "engine",
                    "total_duration", "total_distance", "speed", "positions.attributes")
            .join("devices", "positions.id", "=", "devices.position_id")
            .where("device_id", state.get("deviceId"))
            .first();
            
        MotionHandler.handle(state, attributes, newPosition);
        DurationHandler.handle(state, oldPosition, newPosition);
        DistanceHandler.handle(state, attributes, oldPosition, newPosition);
        AccelerationHandler.handle(state, attributes, oldPosition, newPosition);
        EngineHandler.handle(attributes, oldPosition, newPosition);
        PositionHandler.handle(state, attributes, newPosition);

        this.data.putAll(newPosition);
        if (((Boolean) state.get("valid"))) {
            state = DB.table("positions").create(newPosition);
            Map<String, Object> input = new HashMap<>();
            input.put("position_id", state.get("id"));
            DB.table("devices").where("id", state.get("device_id")).update(input);
        }
        this.channel = "user." + oldPosition.get("user_id") + ".devices";
    }
    
    @Override
    public String channel() {
        return privateChannel(channel);
    }
    
    @Override
    public String event() {
        return "client-device.update.state";
    }
    
    @Override
    public Object data() {
        return data;
    }
}
