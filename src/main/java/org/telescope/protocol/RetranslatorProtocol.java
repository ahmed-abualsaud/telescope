
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class RetranslatorProtocol extends BaseProtocol {

    public RetranslatorProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new RetranslatorFrameDecoder());
                pipeline.addLast(new RetranslatorProtocolDecoder(RetranslatorProtocol.this));
            }
        });
    }

}
