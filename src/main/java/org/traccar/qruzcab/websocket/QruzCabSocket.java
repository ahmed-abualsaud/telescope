package org.traccar.qruzcab.websocket;

import org.traccar.qruzcab.QruzCab;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;

import java.util.HashMap;
import java.util.Map;

public class QruzCabSocket extends WebSocketAdapter implements QruzCabSocketManager.QruzCabSocketListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(QruzCabSocket.class);
    private final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
    
    private final long driverId;
    private final boolean producer;

    public QruzCabSocket(long driverId, boolean producer) {
        this.driverId = driverId;
        this.producer = producer;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        getRemote().sendString("MESSAGE: Connected Successfully", null);
        QruzCab.getSocketManager().addListener(driverId, this);
    }
    
    @Override
    public void onUpdateDriverLocation(String message) {
        if (!producer) {
            getRemote().sendString(message, null);
        }
    }
    
    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        try {
            Map<String, Object> data = Context.getObjectMapper().readValue(message, typeRef);
            QruzCab.table("drivers").where("id", driverId).update(data);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Socket JSON formatting error", e);
        }
        QruzCab.getSocketManager().broadcast(driverId, message);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        QruzCab.getSocketManager().removeListener(driverId, this);
    }
}
