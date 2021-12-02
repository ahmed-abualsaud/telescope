package org.traccar.qruzcab.websocket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

public class QruzCabSocketManager {

    private final Map<Long, Set<QruzCabSocketListener>> listeners;
    
    public QruzCabSocketManager() {
        listeners = new ConcurrentHashMap<>();
    }
    
    public interface QruzCabSocketListener {
        void onUpdateDriverLocation(String message);
    }
    
    public synchronized void broadcast(long driverId, String message) {
        if (listeners.containsKey(driverId)) {
            for (QruzCabSocketListener listener : listeners.get(driverId)) {
                listener.onUpdateDriverLocation(message);
            }
        }
    }
    
    public synchronized void addListener(long driverId, QruzCabSocketListener listener) {
        if (!listeners.containsKey(driverId)) {
            listeners.put(driverId, new HashSet<>());
        }
        listeners.get(driverId).add(listener);
    }

    public synchronized void removeListener(long driverId, QruzCabSocketListener listener) {
        if (!listeners.containsKey(driverId)) {
            listeners.put(driverId, new HashSet<>());
        }
        listeners.get(driverId).remove(listener);
    }
}

