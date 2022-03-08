
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class Gps056Protocol extends BaseProtocol {

    public Gps056Protocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new Gps056FrameDecoder());
                pipeline.addLast(new Gps056ProtocolDecoder(Gps056Protocol.this));
            }
        });
    }

}
