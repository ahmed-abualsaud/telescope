
package org.telescope.protocol;

import io.netty.handler.codec.string.StringEncoder;

import org.telescope.config.Keys;
import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.Context;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class H02Protocol extends BaseProtocol {

    public H02Protocol() {
        setSupportedDataCommands(
                Command.TYPE_ALARM_ARM,
                Command.TYPE_ALARM_DISARM,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_POSITION_PERIODIC
        );
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                int messageLength = Context.getConfig().getInteger(Keys.PROTOCOL_MESSAGE_LENGTH.withPrefix(getName()));
                pipeline.addLast(new H02FrameDecoder(messageLength));
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new H02ProtocolEncoder(H02Protocol.this));
                pipeline.addLast(new H02ProtocolDecoder(H02Protocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new H02ProtocolEncoder(H02Protocol.this));
                pipeline.addLast(new H02ProtocolDecoder(H02Protocol.this));
            }
        });
    }
}
