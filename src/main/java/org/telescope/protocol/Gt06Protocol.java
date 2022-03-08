
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class Gt06Protocol extends BaseProtocol {

    public Gt06Protocol() {
        setSupportedDataCommands(
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_CUSTOM);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new Gt06FrameDecoder());
                pipeline.addLast(new Gt06ProtocolEncoder(Gt06Protocol.this));
                pipeline.addLast(new Gt06ProtocolDecoder(Gt06Protocol.this));
            }
        });
    }

}
