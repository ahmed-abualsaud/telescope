
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.FixedLengthFrameDecoder;

public class M2mProtocol extends BaseProtocol {

    public M2mProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new FixedLengthFrameDecoder(23));
                pipeline.addLast(new M2mProtocolDecoder(M2mProtocol.this));
            }
        });
    }

}
