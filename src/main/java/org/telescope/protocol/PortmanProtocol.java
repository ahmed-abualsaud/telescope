
package org.telescope.protocol;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class PortmanProtocol extends BaseProtocol {

    public PortmanProtocol() {
        setSupportedDataCommands(
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LineBasedFrameDecoder(1024));
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new PortmanProtocolEncoder(PortmanProtocol.this));
                pipeline.addLast(new PortmanProtocolDecoder(PortmanProtocol.this));
            }
        });
    }

}
