
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class NvsProtocol extends BaseProtocol {

    public NvsProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new NvsFrameDecoder());
                pipeline.addLast(new NvsProtocolDecoder(NvsProtocol.this));
            }
        });
    }

}
