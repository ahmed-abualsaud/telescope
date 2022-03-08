
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class NavigilProtocol extends BaseProtocol {

    public NavigilProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new NavigilFrameDecoder());
                pipeline.addLast(new NavigilProtocolDecoder(NavigilProtocol.this));
            }
        });
    }

}
