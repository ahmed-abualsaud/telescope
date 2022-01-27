package org.traccar.api.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.traccar.Context;
import org.traccar.qruzcab.QruzCab;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;
import java.util.HashMap;

public class CabUpdateLocation implements EventManager.Event {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CabUpdateLocation.class);
    private final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
    
    @Override
    public void handle(String channel, String message) {
        try {
            Map<String, Object> data = Context.getObjectMapper().readValue(message, typeRef);
            QruzCab.table("drivers").where("id", channel.split("\\.")[2]).update(data);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Socket JSON formatting error", e);
        }
        Context.getWebsocketManager().broadcast(channel, eventName(), message);
    }
    
    @Override
    public String eventName() {
        return "cab.driver.update.location";
    }
}
