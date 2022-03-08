
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class BceProtocol extends BaseProtocol {

    public BceProtocol() {
        setSupportedDataCommands(
                Command.TYPE_OUTPUT_CONTROL);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new BceFrameDecoder());
                pipeline.addLast(new BceProtocolEncoder(BceProtocol.this));
                pipeline.addLast(new BceProtocolDecoder(BceProtocol.this));
            }
        });
    }

}
