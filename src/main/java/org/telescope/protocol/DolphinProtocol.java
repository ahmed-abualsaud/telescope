
package org.telescope.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class DolphinProtocol extends BaseProtocol {

    public DolphinProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 4096, 20, 4, 4, 0, true));
                pipeline.addLast(new DolphinProtocolDecoder(DolphinProtocol.this));
            }
        });
    }

}
