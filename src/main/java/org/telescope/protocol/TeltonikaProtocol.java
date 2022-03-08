
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class TeltonikaProtocol extends BaseProtocol {

    public TeltonikaProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new TeltonikaFrameDecoder());
                pipeline.addLast(new TeltonikaProtocolEncoder(TeltonikaProtocol.this));
                pipeline.addLast(new TeltonikaProtocolDecoder(TeltonikaProtocol.this, false));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new TeltonikaProtocolEncoder(TeltonikaProtocol.this));
                pipeline.addLast(new TeltonikaProtocolDecoder(TeltonikaProtocol.this, true));
            }
        });
    }

}
