package org.traccar.websocket;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketManager {

    private final Map<String, Set<WebsocketListener>> listeners;
    
    public WebsocketManager() {
        listeners = new ConcurrentHashMap<>();
    }
    
    public interface WebsocketListener {
        void onBroadCast(String message);
    }
    
    public synchronized void broadcast(String channel, String message) {
        if (listeners.containsKey(channel)) {
            for (WebsocketListener listener : listeners.get(channel)) {
                listener.onBroadCast(message);
            }
        }
    }
    
    public synchronized void subscribe(String channel, WebsocketListener listener) {
        if (!listeners.containsKey(channel)) {
            listeners.put(channel, new HashSet<>());
        }
        listeners.get(channel).add(listener);
    }

    public synchronized void unsubscribe(String channel, WebsocketListener listener) {
        if (!listeners.containsKey(channel)) {
            listeners.put(channel, new HashSet<>());
        }
        listeners.get(channel).remove(listener);
    }
}
