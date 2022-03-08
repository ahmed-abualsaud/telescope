
package org.telescope.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class T800xProtocol extends BaseProtocol {

    public T800xProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 3, 2, -5, 0));
                pipeline.addLast(new T800xProtocolEncoder(T800xProtocol.this));
                pipeline.addLast(new T800xProtocolDecoder(T800xProtocol.this));
            }
        });
    }

}
