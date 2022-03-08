
package org.telescope.handler;

import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.database.DataManager;
import org.telescope.model.Position;
import org.telescope.server.BaseDataHandler;

@ChannelHandler.Sharable
public class DefaultDataHandler extends BaseDataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDataHandler.class);

    private final DataManager dataManager;

    public DefaultDataHandler(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    protected Position handlePosition(Position position) {

        try {
            dataManager.addObject(position);
        } catch (Exception error) {
            LOGGER.warn("Failed to store position", error);
        }

        return position;
    }

}
