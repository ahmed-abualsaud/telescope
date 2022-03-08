
package org.telescope.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class TotemProtocol extends BaseProtocol {

    public TotemProtocol() {
        setSupportedDataCommands(
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_ENGINE_STOP
        );
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new TotemFrameDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new TotemProtocolEncoder(TotemProtocol.this));
                pipeline.addLast(new TotemProtocolDecoder(TotemProtocol.this));
            }
        });
    }

}
