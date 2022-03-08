
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class PstProtocol extends BaseProtocol {

    public PstProtocol() {
        setSupportedDataCommands(
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME);
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new PstProtocolEncoder(PstProtocol.this));
                pipeline.addLast(new PstProtocolDecoder(PstProtocol.this));
            }
        });
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new PstFrameEncoder());
                pipeline.addLast(new PstFrameDecoder());
                pipeline.addLast(new PstProtocolEncoder(PstProtocol.this));
                pipeline.addLast(new PstProtocolDecoder(PstProtocol.this));
            }
        });
    }

}
