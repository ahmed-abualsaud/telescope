
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class UlbotechProtocol extends BaseProtocol {

    public UlbotechProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new UlbotechFrameDecoder());
                pipeline.addLast(new UlbotechProtocolEncoder(UlbotechProtocol.this));
                pipeline.addLast(new UlbotechProtocolDecoder(UlbotechProtocol.this));
            }
        });
    }

}
