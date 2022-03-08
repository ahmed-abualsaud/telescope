
package org.telescope.javel.framework.service.notificator;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.config.Keys;
import org.telescope.javel.framework.notification.NotificationFormatter;
import org.telescope.model.Event;
import org.telescope.model.Position;
import org.telescope.model.User;
import org.telescope.server.Context;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;

public class NotificatorPushover extends Notificator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificatorPushover.class);

    private final String url;
    private final String token;
    private final String user;

    public static class Message {
        @JsonProperty("token")
        private String token;
        @JsonProperty("user")
        private String user;
        @JsonProperty("device")
        private String device;
        @JsonProperty("message")
        private String message;
    }

    public NotificatorPushover() {
        url = "https://api.pushover.net/1/messages.json";
        token = Context.getConfig().getString(Keys.NOTIFICATOR_PUSHOVER_TOKEN);
        user = Context.getConfig().getString(Keys.NOTIFICATOR_PUSHOVER_USER);
    }

    @Override
    public void sendSync(long userId, Event event, Position position) {

        final User user = Context.getPermissionsManager().getUser(userId);

        String device = "";

        if (user.getAttributes().containsKey("notificator.pushover.device")) {
            device = user.getString("notificator.pushover.device").replaceAll(" *, *", ",");
        }

        if (token == null) {
            LOGGER.warn("Pushover token not found");
            return;
        }

        if (this.user == null) {
            LOGGER.warn("Pushover user not found");
            return;
        }

        Message message = new Message();
        message.token = token;
        message.user = this.user;
        message.device = device;
        message.message = NotificationFormatter.formatShortMessage(userId, event, position);

        Context.getClient().target(url).request()
                .async().post(Entity.json(message), new InvocationCallback<Object>() {
            @Override
            public void completed(Object o) {
            }

            @Override
            public void failed(Throwable throwable) {
                LOGGER.warn("Pushover API error", throwable);
            }
        });
    }

    @Override
    public void sendAsync(long userId, Event event, Position position) {
        sendSync(userId, event, position);
    }

}
