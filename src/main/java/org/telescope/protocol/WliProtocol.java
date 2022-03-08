
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class WliProtocol extends BaseProtocol {

    public WliProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new WliFrameDecoder());
                pipeline.addLast(new WliProtocolDecoder(WliProtocol.this));
            }
        });
    }

}
