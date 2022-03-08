package org.telescope.javel.framework.websocket;

import java.time.Duration;

import org.telescope.config.Keys;
import org.telescope.server.Context;
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
