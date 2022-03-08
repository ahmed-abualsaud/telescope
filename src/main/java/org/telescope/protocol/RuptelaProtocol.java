
package org.telescope.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class RuptelaProtocol extends BaseProtocol {

    public RuptelaProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_REQUEST_PHOTO,
                Command.TYPE_CONFIGURATION,
                Command.TYPE_GET_VERSION,
                Command.TYPE_FIRMWARE_UPDATE,
                Command.TYPE_OUTPUT_CONTROL,
                Command.TYPE_SET_CONNECTION,
                Command.TYPE_SET_ODOMETER);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 2, 0));
                pipeline.addLast(new RuptelaProtocolEncoder(RuptelaProtocol.this));
                pipeline.addLast(new RuptelaProtocolDecoder(RuptelaProtocol.this));
            }
        });
    }

}
