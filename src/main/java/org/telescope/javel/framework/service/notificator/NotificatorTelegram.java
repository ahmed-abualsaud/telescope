
package org.telescope.javel.framework.service.notificator;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.model.User;
import org.telescope.config.Keys;
import org.telescope.javel.framework.notification.NotificationFormatter;
import org.telescope.model.Event;
import org.telescope.model.Position;
import org.telescope.server.Context;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;

public class NotificatorTelegram extends Notificator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificatorTelegram.class);

    private final String urlSendText;
    private final String urlSendLocation;
    private final String chatId;
    private final boolean sendLocation;

    public static class TextMessage {
        @JsonProperty("chat_id")
        private String chatId;
        @JsonProperty("text")
        private String text;
        @JsonProperty("parse_mode")
        private String parseMode = "html";
    }

    public static class LocationMessage {
        @JsonProperty("chat_id")
        private String chatId;
        @JsonProperty("latitude")
        private double latitude;
        @JsonProperty("longitude")
        private double longitude;
        @JsonProperty("horizontal_accuracy")
        private double accuracy;
        @JsonProperty("bearing")
        private int bearing;
    }

    public NotificatorTelegram() {
        urlSendText = String.format(
                "https://api.telegram.org/bot%s/sendMessage",
                Context.getConfig().getString(Keys.NOTIFICATOR_TELEGRAM_KEY));
        urlSendLocation = String.format(
                "https://api.telegram.org/bot%s/sendLocation",
                Context.getConfig().getString(Keys.NOTIFICATOR_TELEGRAM_KEY));
        chatId = Context.getConfig().getString(Keys.NOTIFICATOR_TELEGRAM_CHAT_ID);
        sendLocation = Context.getConfig().getBoolean(Keys.NOTIFICATOR_TELEGRAM_SEND_LOCATION);
    }

    private void executeRequest(String url, Object message) {
        Context.getClient().target(url).request()
                .async().post(Entity.json(message), new InvocationCallback<Object>() {
            @Override
            public void completed(Object o) {
            }

            @Override
            public void failed(Throwable throwable) {
                LOGGER.warn("Telegram API error", throwable);
            }
        });
    }

    private LocationMessage createLocationMessage(String messageChatId, Position position) {
        LocationMessage locationMessage = new LocationMessage();
        locationMessage.chatId = messageChatId;
        locationMessage.latitude = position.getLatitude();
        locationMessage.longitude = position.getLongitude();
        locationMessage.bearing = (int) Math.ceil(position.getCourse());
        locationMessage.accuracy = position.getAccuracy();
        return locationMessage;
    }

    @Override
    public void sendSync(long userId, Event event, Position position) {
        User user = Context.getPermissionsManager().getUser(userId);
        TextMessage message = new TextMessage();
        message.chatId = user.getString("telegramChatId");
        if (message.chatId == null) {
            message.chatId = chatId;
        }
        message.text = NotificationFormatter.formatShortMessage(userId, event, position);
        executeRequest(urlSendText, message);
        if (sendLocation && position != null) {
            executeRequest(urlSendLocation, createLocationMessage(message.chatId, position));
        }
    }

    @Override
    public void sendAsync(long userId, Event event, Position position) {
        sendSync(userId, event, position);
    }

}
