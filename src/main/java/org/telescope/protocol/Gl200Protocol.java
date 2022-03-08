
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.string.StringEncoder;

public class Gl200Protocol extends BaseProtocol {

    public Gl200Protocol() {
        setSupportedDataCommands(
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_IDENTIFICATION,
                Command.TYPE_REBOOT_DEVICE);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new Gl200FrameDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new Gl200ProtocolEncoder(Gl200Protocol.this));
                pipeline.addLast(new Gl200ProtocolDecoder(Gl200Protocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new Gl200ProtocolEncoder(Gl200Protocol.this));
                pipeline.addLast(new Gl200ProtocolDecoder(Gl200Protocol.this));
            }
        });
    }

}
