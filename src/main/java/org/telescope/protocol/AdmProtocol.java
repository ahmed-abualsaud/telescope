
package org.telescope.protocol;

import io.netty.handler.codec.string.StringEncoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class AdmProtocol extends BaseProtocol {

    public AdmProtocol() {
        setSupportedDataCommands(
                Command.TYPE_GET_DEVICE_STATUS,
                Command.TYPE_CUSTOM);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new AdmFrameDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new AdmProtocolEncoder(AdmProtocol.this));
                pipeline.addLast(new AdmProtocolDecoder(AdmProtocol.this));
            }
        });
    }

}
