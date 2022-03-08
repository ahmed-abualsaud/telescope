
package org.telescope.handler;

import io.netty.channel.ChannelHandler;

import org.telescope.model.Position;
import org.telescope.server.BaseDataHandler;

@ChannelHandler.Sharable
public class MotionHandler extends BaseDataHandler {

    private double speedThreshold;

    public MotionHandler(double speedThreshold) {
        this.speedThreshold = speedThreshold;
    }

    @Override
    protected Position handlePosition(Position position) {
        if (!position.getAttributes().containsKey(Position.KEY_MOTION)) {
            position.set(Position.KEY_MOTION, position.getSpeed() > speedThreshold);
        }
        return position;
    }

}
