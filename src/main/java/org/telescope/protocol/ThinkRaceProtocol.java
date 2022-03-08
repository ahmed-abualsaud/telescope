
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ThinkRaceProtocol extends BaseProtocol {

    public ThinkRaceProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 2 + 12 + 1 + 1, 2, 2, 0));
                pipeline.addLast(new ThinkRaceProtocolDecoder(ThinkRaceProtocol.this));
            }
        });
    }

}
