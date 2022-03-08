
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class RoboTrackProtocol extends BaseProtocol {

    public RoboTrackProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new RoboTrackFrameDecoder());
                pipeline.addLast(new RoboTrackProtocolDecoder(RoboTrackProtocol.this));
            }
        });
    }

}
