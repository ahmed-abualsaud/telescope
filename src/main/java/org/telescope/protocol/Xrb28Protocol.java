
package org.telescope.protocol;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import java.nio.charset.StandardCharsets;

public class Xrb28Protocol extends BaseProtocol {

    public Xrb28Protocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_POSITION_PERIODIC,
                Command.TYPE_ALARM_ARM,
                Command.TYPE_ALARM_DISARM);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LineBasedFrameDecoder(1024));
                pipeline.addLast(new StringEncoder(StandardCharsets.ISO_8859_1));
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new Xrb28ProtocolEncoder(Xrb28Protocol.this));
                pipeline.addLast(new Xrb28ProtocolDecoder(Xrb28Protocol.this));
            }
        });
    }

}
