
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.string.StringDecoder;

public class SabertekProtocol extends BaseProtocol {

    public SabertekProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new SabertekFrameDecoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new SabertekProtocolDecoder(SabertekProtocol.this));
            }
        });
    }

}
