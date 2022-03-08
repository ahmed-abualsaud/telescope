
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class Gt02Protocol extends BaseProtocol {

    public Gt02Protocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(256, 2, 1, 2, 0));
                pipeline.addLast(new Gt02ProtocolDecoder(Gt02Protocol.this));
            }
        });
    }

}
