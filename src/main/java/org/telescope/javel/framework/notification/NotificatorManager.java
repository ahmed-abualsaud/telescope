
package org.telescope.javel.framework.notification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.javel.framework.service.notificator.Notificator;
import org.telescope.javel.framework.service.notificator.NotificatorFirebase;
import org.telescope.javel.framework.service.notificator.NotificatorMail;
import org.telescope.javel.framework.service.notificator.NotificatorNull;
import org.telescope.javel.framework.service.notificator.NotificatorPushover;
import org.telescope.javel.framework.service.notificator.NotificatorSms;
import org.telescope.javel.framework.service.notificator.NotificatorTelegram;
import org.telescope.javel.framework.service.notificator.NotificatorTraccar;
import org.telescope.javel.framework.service.notificator.NotificatorWeb;
import org.telescope.model.Typed;
import org.telescope.server.Context;

public final class NotificatorManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificatorManager.class);

    private static final Notificator NULL_NOTIFICATOR = new NotificatorNull();

    private final Map<String, Notificator> notificators = new HashMap<>();

    public NotificatorManager() {
        final String[] types = Context.getConfig().getString("notificator.types", "").split(",");
        for (String type : types) {
            String defaultNotificator = "";
            switch (type) {
                case "web":
                    defaultNotificator = NotificatorWeb.class.getCanonicalName();
                    break;
                case "mail":
                    defaultNotificator = NotificatorMail.class.getCanonicalName();
                    break;
                case "sms":
                    defaultNotificator = NotificatorSms.class.getCanonicalName();
                    break;
                case "firebase":
                    defaultNotificator = NotificatorFirebase.class.getCanonicalName();
                    break;
                case "telescope":
                    defaultNotificator = NotificatorTraccar.class.getCanonicalName();
                    break;
                case "telegram":
                    defaultNotificator = NotificatorTelegram.class.getCanonicalName();
                    break;
                case "pushover":
                    defaultNotificator = NotificatorPushover.class.getCanonicalName();
                    break;
                default:
                    break;
            }
            final String className = Context.getConfig()
                    .getString("notificator." + type + ".class", defaultNotificator);
            try {
                notificators.put(type, (Notificator) Class.forName(className).newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOGGER.warn("Unable to load notificator class for " + type + " " + className + " " + e.getMessage());
            }
        }
    }

    public Notificator getNotificator(String type) {
        final Notificator notificator = notificators.get(type);
        if (notificator == null) {
            LOGGER.warn("No notificator configured for type : " + type);
            return NULL_NOTIFICATOR;
        }
        return notificator;
    }

    public Set<Typed> getAllNotificatorTypes() {
        Set<Typed> result = new HashSet<>();
        for (String notificator : notificators.keySet()) {
            result.add(new Typed(notificator));
        }
        return result;
    }

}
