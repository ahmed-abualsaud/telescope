
package org.telescope.protocol;

import org.telescope.config.Keys;
import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.Context;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

public class WialonProtocol extends BaseProtocol {

    public WialonProtocol() {
        setSupportedDataCommands(
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_SEND_USSD,
                Command.TYPE_IDENTIFICATION,
                Command.TYPE_OUTPUT_CONTROL);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LineBasedFrameDecoder(4 * 1024));
                boolean utf8 = Context.getConfig().getBoolean(Keys.PROTOCOL_UTF8.withPrefix(getName()));
                if (utf8) {
                    pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                    pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                } else {
                    pipeline.addLast(new StringEncoder());
                    pipeline.addLast(new StringDecoder());
                }
                pipeline.addLast(new WialonProtocolEncoder(WialonProtocol.this));
                pipeline.addLast(new WialonProtocolDecoder(WialonProtocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LineBasedFrameDecoder(4 * 1024));
                boolean utf8 = Context.getConfig().getBoolean(Keys.PROTOCOL_UTF8.withPrefix(getName()));
                if (utf8) {
                    pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                    pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                } else {
                    pipeline.addLast(new StringEncoder());
                    pipeline.addLast(new StringDecoder());
                }
                pipeline.addLast(new WialonProtocolEncoder(WialonProtocol.this));
                pipeline.addLast(new WialonProtocolDecoder(WialonProtocol.this));
            }
        });
    }

}
