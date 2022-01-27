package org.traccar.api.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.LinkedHashMap;

import org.traccar.Context;
import org.traccar.config.Keys;

public class EventManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventManager.class);
    private final Map<String, Event> events = new LinkedHashMap<>();

    public EventManager() {
        try {
            String path = Context.getConfig().getString(Keys.APP_DIRECTORY);
            path += "/telescope/src/main/java/org/traccar/api/event";
            String pack = "org.traccar.api.event";
            File[] files = new File(path).listFiles();
            String fileName, clazz;
            Event event;
        
            for (int i = 0; i < files.length; i++){
                fileName = files[i].getName();
                if (files[i].isFile() && fileName.contains(".java") && !fileName.equals("EventManager.java")) {
                    clazz = pack + "." + fileName.substring(0, fileName.length() - 5);
                    event = (Event) Class.forName(clazz).getConstructor().newInstance();
                    events.put(event.eventName(), event);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Event Manager Error: ", e);
        }
    }
    
    public interface Event {
        String eventName();
        void handle(String channel, String message);
    }
    
    public void handle(String channel, String event, String message) {
        if (events.containsKey(event)) {
            events.get(event).handle(channel, message);
        }
    }
}
