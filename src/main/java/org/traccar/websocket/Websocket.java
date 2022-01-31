package org.traccar.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import org.traccar.Context;

public class Websocket extends WebSocketAdapter implements WebsocketManager.WebsocketListener{

    private static final Logger LOGGER = LoggerFactory.getLogger(Websocket.class);

    private final String channel;
    private final String event;
    private final boolean producer;

    public Websocket(String channel, String event, boolean producer) {
        this.channel = channel;
        this.event = event;
        this.producer = producer;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        sendMessage("MESSAGE: Connected Successfully");
        LOGGER.info("MESSAGE: Connected Successfully");
        Context.getWebsocketManager().addListener(channel, event, this);
    }
    
    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        LOGGER.info(message);
        Context.getEventManager().handle(channel, event, message);
    }
    
    @Override
    public void onBroadCast(Object message) {
        if (!producer) {
            sendMessage(message);
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        LOGGER.info(reason);
        Context.getWebsocketManager().removeListener(channel, event, this);
    }
    
    private void sendMessage(Object message) {
        getRemote().sendString(message.toString(), null);
    }
}
