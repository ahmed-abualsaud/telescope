
package org.telescope.javel.framework.service.notificator;

import org.telescope.model.Event;
import org.telescope.model.Position;
import org.telescope.server.Context;

public final class NotificatorWeb extends Notificator {

    @Override
    public void sendSync(long userId, Event event, Position position) {
        Context.getConnectionManager().updateEvent(userId, event);
    }

}
