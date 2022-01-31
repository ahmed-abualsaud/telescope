package org.traccar.api.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.traccar.Context;
import org.traccar.database.DB;

import java.util.Map;
import java.util.HashMap;

public class DeviceUpdateState implements EventManager.Event {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceUpdateState.class);
    
    @Override
    public void handle(String channel, Object message) {
        LOGGER.info(message.toString());
        Map<String, Object> data = Context.getObjectMapper().convertValue(message, Map.class);
        data.remove("type");
        data.remove("outdated");
        data.put("device_id", data.remove("deviceId"));
        data.put("attributes", data.remove("attributes").toString());
        Object user_id = DB.table("devices").find(data.get("device_id")).get("user_id");
        data = DB.table("positions").create(data);
        Map<String, Object> input = new HashMap<>();
        input.put("position_id", data.get("id"));
        DB.table("devices").where("id", data.get("device_id")).update(input);
        Context.getWebsocketManager().broadcast("user." + user_id + ".devices", eventName(), message);
    }
    
    @Override
    public String eventName() {
        return "device.update.state";
    }
}
