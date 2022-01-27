package org.traccar.api.event;

import org.traccar.Context;

public class SeatsUpdateLocation implements EventManager.Event {
    
    @Override
    public void handle(String channel, String message) {
        Context.getWebsocketManager().broadcast(channel, eventName(), message);
    }
    
    @Override
    public String eventName() {
        return "seats.driver.update.location";
    }
}
