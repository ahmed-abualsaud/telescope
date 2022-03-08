
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class ArnaviProtocol extends BaseProtocol {

    public ArnaviProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new ArnaviFrameDecoder());
                pipeline.addLast(new ArnaviProtocolDecoder(ArnaviProtocol.this));
            }
        });
    }

}
