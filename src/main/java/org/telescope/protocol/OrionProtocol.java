
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class OrionProtocol extends BaseProtocol {

    public OrionProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new OrionFrameDecoder());
                pipeline.addLast(new OrionProtocolDecoder(OrionProtocol.this));
            }
        });
    }

}
