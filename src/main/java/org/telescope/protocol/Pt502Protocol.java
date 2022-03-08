
package org.telescope.protocol;

import io.netty.handler.codec.string.StringEncoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class Pt502Protocol extends BaseProtocol {

    public Pt502Protocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_SET_TIMEZONE,
                Command.TYPE_ALARM_SPEED,
                Command.TYPE_OUTPUT_CONTROL,
                Command.TYPE_REQUEST_PHOTO);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new Pt502FrameDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new Pt502ProtocolEncoder(Pt502Protocol.this));
                pipeline.addLast(new Pt502ProtocolDecoder(Pt502Protocol.this));
            }
        });
    }

}
