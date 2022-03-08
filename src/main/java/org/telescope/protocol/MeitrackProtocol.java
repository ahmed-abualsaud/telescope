
package org.telescope.protocol;

import io.netty.handler.codec.string.StringEncoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class MeitrackProtocol extends BaseProtocol {

    public MeitrackProtocol() {
        setSupportedDataCommands(
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_ALARM_ARM,
                Command.TYPE_ALARM_DISARM,
                Command.TYPE_REQUEST_PHOTO,
                Command.TYPE_SEND_SMS);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new MeitrackFrameDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new MeitrackProtocolEncoder(MeitrackProtocol.this));
                pipeline.addLast(new MeitrackProtocolDecoder(MeitrackProtocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new MeitrackProtocolEncoder(MeitrackProtocol.this));
                pipeline.addLast(new MeitrackProtocolDecoder(MeitrackProtocol.this));
            }
        });
    }

}
