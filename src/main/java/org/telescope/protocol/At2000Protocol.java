
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class At2000Protocol extends BaseProtocol {

    public At2000Protocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new At2000FrameDecoder());
                pipeline.addLast(new At2000ProtocolDecoder(At2000Protocol.this));
            }
        });
    }

}
