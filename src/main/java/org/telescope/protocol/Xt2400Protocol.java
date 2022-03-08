
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class Xt2400Protocol extends BaseProtocol {

    public Xt2400Protocol() {
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new Xt2400ProtocolDecoder(Xt2400Protocol.this));
            }
        });
    }

}
