package org.traccar.websocket.driver;

public abstract class WebsocketDriver {

    protected boolean producer;
    
    public boolean isProducer() {
        return producer;
    }

    public void beConsumer() {
        this.producer = false;
    }

    public void beProducer() {
        this.producer = true;
    }

    public abstract String connect();
    public abstract void close(String reason);

    public abstract String handle(String message);
    public abstract String buildMessage(String channel, String event, Object data);
}
