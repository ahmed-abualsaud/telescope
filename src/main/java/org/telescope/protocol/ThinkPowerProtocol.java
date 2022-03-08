
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ThinkPowerProtocol extends BaseProtocol {

    public ThinkPowerProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 2, 2, 2, 0));
                pipeline.addLast(new ThinkPowerProtocolDecoder(ThinkPowerProtocol.this));
            }
        });
    }

}
