
package org.telescope.handler.events;

import java.util.Map;

import org.telescope.model.Event;
import org.telescope.model.Position;
import org.telescope.server.BaseDataHandler;
import org.telescope.server.Context;

public abstract class BaseEventHandler extends BaseDataHandler {

    @Override
    protected Position handlePosition(Position position) {
        Map<Event, Position> events = analyzePosition(position);
        if (events != null && Context.getNotificationManager() != null) {
            Context.getNotificationManager().updateEvents(events);
            Context.getConnectionManager().sendEvents(position.getDeviceId(), events);
        }
        return position;
    }

    protected abstract Map<Event, Position> analyzePosition(Position position);

}
