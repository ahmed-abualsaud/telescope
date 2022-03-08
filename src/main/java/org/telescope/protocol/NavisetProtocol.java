
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class NavisetProtocol extends BaseProtocol {

    public NavisetProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new NavisetFrameDecoder());
                pipeline.addLast(new NavisetProtocolDecoder(NavisetProtocol.this));
            }
        });
    }

}
