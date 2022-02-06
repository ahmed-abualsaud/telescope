package org.traccar.websocket;

import org.traccar.Context;
import org.traccar.config.Keys;
import org.traccar.websocket.driver.*;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;


public class Websocket extends WebSocketAdapter implements WebsocketManager.WebsocketListener{

    private String message;
    private WebsocketDriver driver;
    
    public Websocket() {
        switch (Context.getConfig().getString(Keys.WEBSOCKET_DRIVER)) {
            case "pusher":
                this.driver = new Pusher(this);
            break;
            default:
                this.driver = null;
        }
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        sendMessage(driver.connect());
    }
    
    @Override
    public void onWebSocketText(String msg) {
        super.onWebSocketText(msg);
        message = driver.handle(msg);
        if (message != null) {sendMessage(message);} 
    }
    
    @Override
    public void onBroadCast(String message) {
        if(driver.isProducer()) {driver.beConsumer();}
        else {sendMessage(message);}
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        driver.close(reason);
    }

    private void sendMessage(String message) {
        getRemote().sendString(message, null);
    }
}
