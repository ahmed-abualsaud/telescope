
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class Avl301Protocol extends BaseProtocol {

    public Avl301Protocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(256, 2, 1, -3, 0));
                pipeline.addLast(new Avl301ProtocolDecoder(Avl301Protocol.this));
            }
        });
    }

}
