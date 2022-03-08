
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class TopinProtocol extends BaseProtocol {

    public TopinProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new TopinProtocolDecoder(TopinProtocol.this));
            }
        });
    }

}
