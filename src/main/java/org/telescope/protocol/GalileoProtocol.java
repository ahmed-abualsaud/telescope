
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class GalileoProtocol extends BaseProtocol {

    public GalileoProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_OUTPUT_CONTROL);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new GalileoFrameDecoder());
                pipeline.addLast(new GalileoProtocolEncoder(GalileoProtocol.this));
                pipeline.addLast(new GalileoProtocolDecoder(GalileoProtocol.this));
            }
        });
    }

}
