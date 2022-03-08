
package org.telescope.handler.events;

import io.netty.channel.ChannelHandler;
import org.telescope.config.Config;
import org.telescope.config.Keys;
import org.telescope.database.IdentityManager;
import org.telescope.javel.framework.helper.UnitsConverter;
import org.telescope.model.Event;
import org.telescope.model.Position;

import java.util.Collections;
import java.util.Map;

@ChannelHandler.Sharable
public class BehaviorEventHandler extends BaseEventHandler {

    private final double accelerationThreshold;
    private final double brakingThreshold;

    private final IdentityManager identityManager;

    public BehaviorEventHandler(Config config, IdentityManager identityManager) {
        accelerationThreshold = config.getDouble(Keys.EVENT_BEHAVIOR_ACCELERATION_THRESHOLD);
        brakingThreshold = config.getDouble(Keys.EVENT_BEHAVIOR_BRAKING_THRESHOLD);
        this.identityManager = identityManager;
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {

        Position lastPosition = identityManager.getLastPosition(position.getDeviceId());
        if (lastPosition != null && position.getFixTime().equals(lastPosition.getFixTime())) {
            double acceleration = UnitsConverter.mpsFromKnots(position.getSpeed() - lastPosition.getSpeed()) * 1000
                    / (position.getFixTime().getTime() - lastPosition.getFixTime().getTime());
            if (accelerationThreshold != 0 && acceleration >= accelerationThreshold) {
                Event event = new Event(Event.TYPE_ALARM, position);
                event.set(Position.KEY_ALARM, Position.ALARM_ACCELERATION);
                return Collections.singletonMap(event, position);
            } else if (brakingThreshold != 0 && acceleration <= -brakingThreshold) {
                Event event = new Event(Event.TYPE_ALARM, position);
                event.set(Position.KEY_ALARM, Position.ALARM_BRAKING);
                return Collections.singletonMap(event, position);
            }
        }
        return null;
    }

}
