
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class NavisProtocol extends BaseProtocol {

    public NavisProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new NavisFrameDecoder());
                pipeline.addLast(new NavisProtocolDecoder(NavisProtocol.this));
            }
        });
    }
}
