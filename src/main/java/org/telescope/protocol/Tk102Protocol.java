
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class Tk102Protocol extends BaseProtocol {

    public Tk102Protocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 1 + 1 + 10, 1, 1, 0));
                pipeline.addLast(new Tk102ProtocolDecoder(Tk102Protocol.this));
            }
        });
    }

}
