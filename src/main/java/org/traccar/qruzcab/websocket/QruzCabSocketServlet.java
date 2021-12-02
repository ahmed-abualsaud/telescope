package org.traccar.qruzcab.websocket;

import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.traccar.Context;
import org.traccar.config.Keys;
import java.time.Duration;

public class QruzCabSocketServlet extends JettyWebSocketServlet {

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

            if (req.getParameterMap().containsKey("driverId")) {
                String driverId = req.getParameterMap().get("driverId").get(0);
                if (driverId != null) {
                    if (req.getParameterMap().containsKey("producer")) {
                        String producer = req.getParameterMap().get("producer").get(0);
                        if (producer != null && Boolean.parseBoolean(producer)) {
                            return new QruzCabSocket(Long.parseLong(driverId), true);
                        }
                    }
                    return new QruzCabSocket(Long.parseLong(driverId), false);
                }
            }
            
            return null;
        });
    }

}
