
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class AtrackProtocol extends BaseProtocol {

    public AtrackProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new AtrackFrameDecoder());
                pipeline.addLast(new AtrackProtocolEncoder(AtrackProtocol.this));
                pipeline.addLast(new AtrackProtocolDecoder(AtrackProtocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new AtrackProtocolEncoder(AtrackProtocol.this));
                pipeline.addLast(new AtrackProtocolDecoder(AtrackProtocol.this));
            }
        });
    }

}
