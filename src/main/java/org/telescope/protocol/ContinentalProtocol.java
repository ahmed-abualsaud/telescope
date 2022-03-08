
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ContinentalProtocol extends BaseProtocol {

    public ContinentalProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 2, 2, -4, 0));
                pipeline.addLast(new ContinentalProtocolDecoder(ContinentalProtocol.this));
            }
        });
    }

}
