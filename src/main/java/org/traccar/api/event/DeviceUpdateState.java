package org.traccar.api.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.traccar.Context;

public class DeviceUpdateState implements EventManager.Event {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceUpdateState.class);
    
    @Override
    public void handle(String channel, String message) {
        /*
        try {
            Map<String, Object> data = Context.getObjectMapper().readValue(message, typeRef);
            QruzCab.table("drivers").where("id", channel.split("\\.")[2]).update(data);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Socket JSON formatting error", e);
        }
        */
        LOGGER.info(message);
        Context.getWebsocketManager().broadcast(channel, eventName(), message);
    }
    
    @Override
    public String eventName() {
        return "device.update.state";
    }
}
