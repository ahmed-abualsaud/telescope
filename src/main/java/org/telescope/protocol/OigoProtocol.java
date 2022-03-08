
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class OigoProtocol extends BaseProtocol {

    public OigoProtocol() {
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new OigoProtocolDecoder(OigoProtocol.this));
            }
        });
    }

}
