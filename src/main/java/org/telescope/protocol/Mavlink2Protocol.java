
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class Mavlink2Protocol extends BaseProtocol {

    public Mavlink2Protocol() {
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 1, 1, 10, 0));
                pipeline.addLast(new Mavlink2ProtocolDecoder(Mavlink2Protocol.this));
            }
        });
    }

}
