package org.telescope.app.event;

import java.util.Map;
import org.telescope.javel.framework.event.Event;

public class DeviceUpdateState extends Event {

    private String channel;
    private Map<String, Object> data;
    
    public DeviceUpdateState(String channel, Map<String, Object> data) {
        this.channel = channel;
        this.data = data;
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
