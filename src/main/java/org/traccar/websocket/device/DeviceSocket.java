package org.traccar.websocket.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.database.ConnectionManager;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.model.Position;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DeviceSocket extends WebSocketAdapter implements ConnectionManager.UpdateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceSocket.class);

    private static final String KEY_DEVICES = "devices";
    private static final String KEY_POSITIONS = "positions";
    private static final String KEY_EVENTS = "events";

    private final long userId;

    public DeviceSocket(long userId) {
        this.userId = userId;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);

        Map<String, Collection<?>> data = new HashMap<>();
        data.put(KEY_POSITIONS, Context.getDeviceManager().getInitialState(userId));
        sendData(data);

        Context.getConnectionManager().addListener(userId, this);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);

        Context.getConnectionManager().removeListener(userId, this);
    }

    @Override
    public void onKeepalive() {
        sendData(new HashMap<>());
    }

    @Override
    public void onUpdateDevice(Device device) {
        Map<String, Collection<?>> data = new HashMap<>();
        data.put(KEY_DEVICES, Collections.singletonList(device));
        sendData(data);
    }

    @Override
    public void onUpdatePosition(Position position) {
        Map<String, Collection<?>> data = new HashMap<>();
        data.put(KEY_POSITIONS, Collections.singletonList(position));
        sendData(data);
    }

    @Override
    public void onUpdateEvent(Event event) {
        Map<String, Collection<?>> data = new HashMap<>();
        data.put(KEY_EVENTS, Collections.singletonList(event));
        sendData(data);
    }

    private void sendData(Map<String, Collection<?>> data) {
        if (isConnected()) {
            try {
                getRemote().sendString(Context.getObjectMapper().writeValueAsString(data), null);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Socket JSON formatting error", e);
            }
        }
    }
}
