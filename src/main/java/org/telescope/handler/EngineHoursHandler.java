
package org.telescope.handler;

import io.netty.channel.ChannelHandler;

import org.telescope.database.IdentityManager;
import org.telescope.model.Position;
import org.telescope.server.BaseDataHandler;

@ChannelHandler.Sharable
public class EngineHoursHandler extends BaseDataHandler {

    private final IdentityManager identityManager;

    public EngineHoursHandler(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    @Override
    protected Position handlePosition(Position position) {
        if (!position.getAttributes().containsKey(Position.KEY_HOURS)) {
            Position last = identityManager.getLastPosition(position.getDeviceId());
            if (last != null) {
                long hours = last.getLong(Position.KEY_HOURS);
                if (last.getBoolean(Position.KEY_IGNITION) && position.getBoolean(Position.KEY_IGNITION)) {
                    hours += position.getFixTime().getTime() - last.getFixTime().getTime();
                }
                if (hours != 0) {
                    position.set(Position.KEY_HOURS, hours);
                }
            }
        }
        return position;
    }

}
