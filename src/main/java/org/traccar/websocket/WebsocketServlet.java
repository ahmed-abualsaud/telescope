package org.traccar.websocket;

import java.time.Duration;
import org.traccar.Context;
import org.traccar.config.Keys;
import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;

public class WebsocketServlet extends JettyWebSocketServlet {

    @Override
    public void configure(JettyWebSocketServletFactory factory) {
        factory.setIdleTimeout(Duration.ofMillis(Context.getConfig().getLong(Keys.WEB_TIMEOUT)));
        factory.setCreator((req, resp) -> {
        
            if (!req.getParameterMap().containsKey("token")) {
                return null;
            }
        
            String apiKey = req.getParameterMap().get("token").get(0);
            
            if (!apiKey.equals("cXJ1ejoxMjM0NTY3ODk=")) {
                return null;
            }

            if (req.getParameterMap().containsKey("channel") && 
                req.getParameterMap().containsKey("event")) {
                
                String channel = req.getParameterMap().get("channel").get(0);
                String event = req.getParameterMap().get("event").get(0);
                
                if (channel != null && event != null) {
                    if (req.getParameterMap().containsKey("producer")) {
                    
                        String producer = req.getParameterMap().get("producer").get(0);
                        if (producer != null && producer.toLowerCase().equals("true")) {
                            return new Websocket(channel, event, true);
                        }
                    }
                    return new Websocket(channel, event, false);
                }
            }
            return null;
        });
    }

}
