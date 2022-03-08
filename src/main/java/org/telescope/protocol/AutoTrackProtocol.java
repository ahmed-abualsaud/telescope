
package org.telescope.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;
public class AutoTrackProtocol extends BaseProtocol {

    public AutoTrackProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 1024, 5, 2, 2, 0, true));
                pipeline.addLast(new AutoTrackProtocolDecoder(AutoTrackProtocol.this));
            }
        });
    }

}
