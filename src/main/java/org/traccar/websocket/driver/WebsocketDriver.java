package org.traccar.websocket.driver;

public interface WebsocketDriver {

    boolean isProducer();
    
    void beConsumer();
    void beProducer();

    String connect();
    void close(String reason);

    String handle(String message);
    String buildMessage(String channel, String event, String data);
}
