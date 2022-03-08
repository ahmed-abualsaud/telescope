
package org.telescope.web;

import org.h2.server.web.ConnectionInfo;
import org.h2.server.web.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.config.Keys;
import org.telescope.server.Context;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ConsoleServlet extends WebServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleServlet.class);

    @Override
    public void init() {
        super.init();

        try {
            Field field = WebServlet.class.getDeclaredField("server");
            field.setAccessible(true);
            org.h2.server.web.WebServer server = (org.h2.server.web.WebServer) field.get(this);

            ConnectionInfo connectionInfo = new ConnectionInfo("telescope|"
                    + Context.getConfig().getString(Keys.DATABASE_DRIVER) + "|"
                    + Context.getConfig().getString(Keys.DATABASE_URL) + "|"
                    + Context.getConfig().getString(Keys.DATABASE_USER));

            Method method;

            method = org.h2.server.web.WebServer.class.getDeclaredMethod("updateSetting", ConnectionInfo.class);
            method.setAccessible(true);
            method.invoke(server, connectionInfo);

            method = org.h2.server.web.WebServer.class.getDeclaredMethod("setAllowOthers", boolean.class);
            method.setAccessible(true);
            method.invoke(server, true);

        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.warn("Console reflection error", e);
        }
    }

}
