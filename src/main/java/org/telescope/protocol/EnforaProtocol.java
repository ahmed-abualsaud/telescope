
package org.telescope.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class EnforaProtocol extends BaseProtocol {

    public EnforaProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, -2, 2));
                pipeline.addLast(new EnforaProtocolEncoder(EnforaProtocol.this));
                pipeline.addLast(new EnforaProtocolDecoder(EnforaProtocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new EnforaProtocolEncoder(EnforaProtocol.this));
                pipeline.addLast(new EnforaProtocolDecoder(EnforaProtocol.this));
            }
        });
    }

}
