
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.FixedLengthFrameDecoder;

public class PricolProtocol extends BaseProtocol {

    public PricolProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new FixedLengthFrameDecoder(64));
                pipeline.addLast(new PricolProtocolDecoder(PricolProtocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new PricolProtocolDecoder(PricolProtocol.this));
            }
        });
    }

}
