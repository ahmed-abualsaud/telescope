
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class WristbandProtocol extends BaseProtocol {

    public WristbandProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 3, 2, 3, 0));
                pipeline.addLast(new WristbandProtocolDecoder(WristbandProtocol.this));
            }
        });
    }

}
