
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class TzoneProtocol extends BaseProtocol {

    public TzoneProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(256, 2, 2, 2, 0));
                pipeline.addLast(new TzoneProtocolDecoder(TzoneProtocol.this));
            }
        });
    }

}
