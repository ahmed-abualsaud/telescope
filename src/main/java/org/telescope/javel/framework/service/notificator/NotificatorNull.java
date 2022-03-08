
package org.telescope.javel.framework.service.notificator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.model.Event;
import org.telescope.model.Position;

public final class NotificatorNull extends Notificator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificatorNull.class);

    @Override
    public void sendAsync(long userId, Event event, Position position) {
        LOGGER.warn("You are using null notificatior, please check your configuration, notification not sent");
    }

    @Override
    public void sendSync(long userId, Event event, Position position) {
        LOGGER.warn("You are using null notificatior, please check your configuration, notification not sent");
    }

}
