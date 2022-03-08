
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class Vt200Protocol extends BaseProtocol {

    public Vt200Protocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new Vt200FrameDecoder());
                pipeline.addLast(new Vt200ProtocolDecoder(Vt200Protocol.this));
            }
        });
    }

}
