
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class OmnicommProtocol extends BaseProtocol {

    public OmnicommProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new OmnicommFrameDecoder());
                pipeline.addLast(new OmnicommProtocolDecoder(OmnicommProtocol.this));
            }
        });
    }

}
