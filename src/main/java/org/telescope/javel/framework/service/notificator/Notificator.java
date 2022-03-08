
package org.telescope.javel.framework.service.notificator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.javel.framework.notification.MessageException;
import org.telescope.model.Event;
import org.telescope.model.Position;

public abstract class Notificator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Notificator.class);

    public void sendAsync(final long userId, final Event event, final Position position) {
        new Thread(() -> {
            try {
                sendSync(userId, event, position);
            } catch (MessageException | InterruptedException error) {
                LOGGER.warn("Event send error", error);
            }
        }).start();
    }

    public abstract void sendSync(long userId, Event event, Position position)
        throws MessageException, InterruptedException;

}
