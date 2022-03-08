
package org.telescope.handler.events;

import java.util.Collections;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.telescope.config.Config;
import org.telescope.config.Keys;
import org.telescope.database.IdentityManager;
import org.telescope.model.Event;
import org.telescope.model.Position;

@ChannelHandler.Sharable
public class AlertEventHandler extends BaseEventHandler {

    private final IdentityManager identityManager;
    private final boolean ignoreDuplicateAlerts;

    public AlertEventHandler(Config config, IdentityManager identityManager) {
        this.identityManager = identityManager;
        ignoreDuplicateAlerts = config.getBoolean(Keys.EVENT_IGNORE_DUPLICATE_ALERTS);
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        Object alarm = position.getAttributes().get(Position.KEY_ALARM);
        if (alarm != null) {
            boolean ignoreAlert = false;
            if (ignoreDuplicateAlerts) {
                Position lastPosition = identityManager.getLastPosition(position.getDeviceId());
                if (lastPosition != null && alarm.equals(lastPosition.getAttributes().get(Position.KEY_ALARM))) {
                    ignoreAlert = true;
                }
            }
            if (!ignoreAlert) {
                Event event = new Event(Event.TYPE_ALARM, position);
                event.set(Position.KEY_ALARM, (String) alarm);
                return Collections.singletonMap(event, position);
            }
        }
        return null;
    }

}
