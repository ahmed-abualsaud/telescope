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
        void onBroadCast(Object message);
    }
    
    public synchronized void broadcast(String channel, String event, Object message) {
        String key = getListenerKey(channel, event);
        if (listeners.containsKey(key)) {
            for (WebsocketListener listener : listeners.get(key)) {
                listener.onBroadCast(message);
            }
        }
    }
    
    public synchronized void addListener(String channel, String event, WebsocketListener listener) {
        String key = getListenerKey(channel, event);
        if (!listeners.containsKey(key)) {
            listeners.put(key, new HashSet<>());
        }
        listeners.get(key).add(listener);
    }

    public synchronized void removeListener(String channel, String event, WebsocketListener listener) {
        String key = getListenerKey(channel, event);
        if (!listeners.containsKey(key)) {
            listeners.put(key, new HashSet<>());
        }
        listeners.get(key).remove(listener);
    }
    
    private String getListenerKey(String channel, String event) {
        return channel + "|" + event;
    }
}
