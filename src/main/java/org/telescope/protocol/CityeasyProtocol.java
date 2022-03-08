
package org.telescope.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class CityeasyProtocol extends BaseProtocol {

    public CityeasyProtocol() {
        setSupportedDataCommands(
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_POSITION_PERIODIC,
                Command.TYPE_POSITION_STOP,
                Command.TYPE_SET_TIMEZONE);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 2, 2, -4, 0));
                pipeline.addLast(new CityeasyProtocolEncoder(CityeasyProtocol.this));
                pipeline.addLast(new CityeasyProtocolDecoder(CityeasyProtocol.this));
            }
        });
    }

}
