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
            return new Websocket();
        });
    }

}
