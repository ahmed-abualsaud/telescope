
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class AplicomProtocol extends BaseProtocol {

    public AplicomProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new AplicomFrameDecoder());
                pipeline.addLast(new AplicomProtocolDecoder(AplicomProtocol.this));
            }
        });
    }

}
