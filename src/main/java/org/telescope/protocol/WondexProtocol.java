
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.string.StringEncoder;

public class WondexProtocol extends BaseProtocol {

    public WondexProtocol() {
        setSupportedDataCommands(
                Command.TYPE_GET_DEVICE_STATUS,
                Command.TYPE_GET_MODEM_STATUS,
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_GET_VERSION,
                Command.TYPE_IDENTIFICATION);
        setTextCommandEncoder(new WondexProtocolEncoder(this));
        setSupportedTextCommands(
                Command.TYPE_GET_DEVICE_STATUS,
                Command.TYPE_GET_MODEM_STATUS,
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_GET_VERSION,
                Command.TYPE_IDENTIFICATION);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new WondexFrameDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new WondexProtocolEncoder(WondexProtocol.this));
                pipeline.addLast(new WondexProtocolDecoder(WondexProtocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new WondexProtocolEncoder(WondexProtocol.this));
                pipeline.addLast(new WondexProtocolDecoder(WondexProtocol.this));
            }
        });
    }

}
