
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ObdDongleProtocol extends BaseProtocol {

    public ObdDongleProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1099, 20, 2, 3, 0));
                pipeline.addLast(new ObdDongleProtocolDecoder(ObdDongleProtocol.this));
            }
        });
    }

}
