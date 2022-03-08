
package org.telescope.protocol;

import io.netty.handler.codec.string.StringEncoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class FifotrackProtocol extends BaseProtocol {

    public FifotrackProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_REQUEST_PHOTO);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new FifotrackFrameDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new FifotrackProtocolEncoder(FifotrackProtocol.this));
                pipeline.addLast(new FifotrackProtocolDecoder(FifotrackProtocol.this));
            }
        });
    }

}
