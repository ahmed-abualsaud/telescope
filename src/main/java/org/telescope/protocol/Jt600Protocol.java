
package org.telescope.protocol;

import io.netty.handler.codec.string.StringEncoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class Jt600Protocol extends BaseProtocol {

    public Jt600Protocol() {
        setSupportedDataCommands(
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_SET_TIMEZONE,
                Command.TYPE_REBOOT_DEVICE);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new Jt600FrameDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new Jt600ProtocolEncoder(Jt600Protocol.this));
                pipeline.addLast(new Jt600ProtocolDecoder(Jt600Protocol.this));
            }
        });
    }

}
