
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.string.StringDecoder;

public class VtfmsProtocol extends BaseProtocol {

    public VtfmsProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new VtfmsFrameDecoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new VtfmsProtocolDecoder(VtfmsProtocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new VtfmsProtocolDecoder(VtfmsProtocol.this));
            }
        });
    }

}
