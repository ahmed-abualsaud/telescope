package org.traccar.api.event;

import org.traccar.Context;
import org.traccar.database.DB;

import java.util.Map;
import java.util.HashMap;

public class DeviceUpdateState extends Event {

    private String channel;
    private String data;
    
    public DeviceUpdateState(Object message) {
        this.data = message.toString();
        Map<String, Object> data = Context.jsonDecode(message);
        data.remove("type");
        data.remove("outdated");
        data.put("device_id", data.remove("deviceId"));
        data.put("attributes", data.remove("attributes").toString());
        Object user_id = DB.table("devices").find(data.get("device_id")).get("user_id");
        this.channel = "user." + user_id + ".devices";
        data = DB.table("positions").create(data);
        Map<String, Object> input = new HashMap<>();
        input.put("position_id", data.get("id"));
        DB.table("devices").where("id", data.get("device_id")).update(input);
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
    public String data() {
        return data;
    }
}
