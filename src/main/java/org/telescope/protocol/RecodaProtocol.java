
package org.telescope.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;
public class RecodaProtocol extends BaseProtocol {

    public RecodaProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 1024, 4, 4, -8, 0, true));
                pipeline.addLast(new RecodaProtocolDecoder(RecodaProtocol.this));
            }
        });
    }

}
