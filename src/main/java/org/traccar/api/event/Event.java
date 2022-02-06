package org.traccar.api.event;

public abstract class Event {

    protected String publicChannel(String channel) {
        return channel;
    }

    protected String privateChannel(String channel) {
        return "private-" + channel;
    }

    protected String presenceChannel(String channel) {
        return "presence-" + channel;
    }

    public abstract String channel();
    public abstract String event();
    public abstract String data(); 
}
