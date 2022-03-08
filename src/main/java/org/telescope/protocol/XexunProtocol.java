
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

public class XexunProtocol extends BaseProtocol {

    public XexunProtocol() {
        setSupportedDataCommands(
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                boolean full = Context.getConfig().getBoolean(Keys.PROTOCOL_EXTENDED.withPrefix(getName()));
                if (full) {
                    pipeline.addLast(new LineBasedFrameDecoder(1024)); // tracker bug \n\r
                } else {
                    pipeline.addLast(new XexunFrameDecoder());
                }
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new XexunProtocolEncoder(XexunProtocol.this));
                pipeline.addLast(new XexunProtocolDecoder(XexunProtocol.this, full));
            }
        });
    }

}
