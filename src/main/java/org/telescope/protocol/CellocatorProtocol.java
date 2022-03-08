
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class CellocatorProtocol extends BaseProtocol {

    public CellocatorProtocol() {
        setSupportedDataCommands(
                Command.TYPE_OUTPUT_CONTROL);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new CellocatorFrameDecoder());
                pipeline.addLast(new CellocatorProtocolEncoder(CellocatorProtocol.this));
                pipeline.addLast(new CellocatorProtocolDecoder(CellocatorProtocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new CellocatorProtocolEncoder(CellocatorProtocol.this));
                pipeline.addLast(new CellocatorProtocolDecoder(CellocatorProtocol.this));
            }
        });
    }

}
