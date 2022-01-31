package org.traccar.api.event;

import org.traccar.Context;
import org.traccar.qruzcab.QruzCab;

import java.util.Map;

public class CabUpdateLocation implements EventManager.Event {
    
    @Override
    public void handle(String channel, Object message) {
        Map<String, Object> data = Context.getObjectMapper().convertValue(message, Map.class);
        QruzCab.table("drivers").where("id", channel.split("\\.")[2]).update(data);
        Context.getWebsocketManager().broadcast(channel, eventName(), message);
    }
    
    @Override
    public String eventName() {
        return "cab.driver.update.location";
    }
}
