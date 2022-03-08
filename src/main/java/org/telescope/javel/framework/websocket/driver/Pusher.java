package org.telescope.javel.framework.websocket.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.LinkedHashMap;

import org.telescope.javel.framework.helper.JSON;
import org.telescope.javel.framework.websocket.Websocket;
import org.telescope.server.Context;

public class Pusher extends WebsocketDriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pusher.class);

    private String channel;
    private String message;
    private String socketId;
    private Websocket websocket;

    public Pusher() {}
    public Pusher(Websocket websocket) {
        this.message = "";
        this.channel = "";
        this.producer = false;
        this.websocket = websocket;
        this.socketId = getSocketId();
    }

    @Override
    public String connect() {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> event = new LinkedHashMap<>();
        data.put("socket_id", socketId);
        data.put("activity_timeout", 30);
        event.put("event", "pusher:connection_established");
        event.put("data", data);
        LOGGER.info("Connected Successfully with socket id: " + socketId);
        this.message = JSON.encode(event);
        LOGGER.info("Sending Message: " + message);
        return message;
    }

    @Override
    public void close(String reason) {
        LOGGER.info("Disconnect socket with id: " + socketId + " because of: " + reason);
        Context.getWebsocketManager().unsubscribe(channel, websocket);
    }

    @Override
    public String buildMessage(String channel, String event, Object data) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("data", data);
        message.put("event", event);
        message.put("channel", channel);
        return JSON.encode(message);
    }

    @Override
    public String handle(String message) {
        Map<String, Object> event = JSON.decode(message);
        LOGGER.info("Received Message: " + message);
        if (event.get("event").toString().equals("pusher:ping")) {
            return pong();
        } else if (event.get("event").toString().equals("pusher:subscribe")) {
            return subscribe(event);
        } else if (event.get("event").toString().equals("pusher:unsubscribe")) {
            return unsubscribe();
        } else {
            return broadcast(message);
        }
    }

    private String pong() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("event", "pusher:pong");
        this.message = JSON.encode(event);
        LOGGER.info("Sending Message: " + message);
        return message;
    }

    private String subscribe(Map<String, Object> event) {
        this.channel = JSON.decode(event.get("data")).get("channel").toString();
        Context.getWebsocketManager().subscribe(channel, websocket);
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("event", "pusher_internal:subscription_succeeded");
        msg.put("channel", channel);
        this.message = JSON.encode(msg);
        LOGGER.info("Socket with id: " + socketId + " subscribed to channel: " + channel);
        LOGGER.info("Sending Message: " + message);
        return message;
    }

    private String unsubscribe() {
        Context.getWebsocketManager().unsubscribe(channel, websocket);
        LOGGER.info("Socket with id: " + socketId + " unsubscribed from channel: " + channel);
        return null;
    }

    private String broadcast(String message) {
        beProducer();
        LOGGER.info("Broadcast Message: " + message);
        Context.getWebsocketManager().broadcast(channel, message);
        return null;
    }
    
    public String getSocketId() {
        int random1 = (int) ((Math.random() * (1000000000 - 1)) + 1);
        int random2 = (int) ((Math.random() * (1000000000 - 1)) + 1);
        return random1 + "." + random2;
    }
}
