package org.traccar.websocket.device;

import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.traccar.Context;
import org.traccar.config.Keys;
import java.time.Duration;

public class DeviceSocketServlet extends JettyWebSocketServlet {

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
            
            if (req.getParameterMap().containsKey("userId")) {
                String userId = req.getParameterMap().get("userId").get(0);
                if (userId != null) {
                    return new DeviceSocket(Long.parseLong(userId));
                }
            }
            
            return null;
        });
    }

}
