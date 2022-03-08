
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class MxtProtocol extends BaseProtocol {

    public MxtProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                    pipeline.addLast(new MxtFrameDecoder());
                    pipeline.addLast(new MxtProtocolDecoder(MxtProtocol.this));
                }
        });
    }

}
