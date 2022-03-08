
package org.telescope.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class KhdProtocol extends BaseProtocol {

    public KhdProtocol() {
        setSupportedDataCommands(
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_GET_VERSION,
                Command.TYPE_FACTORY_RESET,
                Command.TYPE_SET_SPEED_LIMIT,
                Command.TYPE_SET_ODOMETER,
                Command.TYPE_POSITION_SINGLE);

        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(512, 3, 2));
                pipeline.addLast(new KhdProtocolEncoder(KhdProtocol.this));
                pipeline.addLast(new KhdProtocolDecoder(KhdProtocol.this));
            }
        });
    }

}
