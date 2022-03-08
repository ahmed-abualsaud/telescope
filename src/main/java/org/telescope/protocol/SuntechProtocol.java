
package org.telescope.protocol;

import io.netty.handler.codec.string.StringEncoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class SuntechProtocol extends BaseProtocol {

    public SuntechProtocol() {
        setSupportedDataCommands(
                Command.TYPE_OUTPUT_CONTROL,
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_ALARM_ARM,
                Command.TYPE_ALARM_DISARM);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new SuntechFrameDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new SuntechProtocolEncoder(SuntechProtocol.this));
                pipeline.addLast(new SuntechProtocolDecoder(SuntechProtocol.this));
            }
        });
    }

}
