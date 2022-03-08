
package org.telescope.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import java.nio.ByteOrder;
public class CastelProtocol extends BaseProtocol {

    public CastelProtocol() {
        setSupportedDataCommands(
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 1024, 2, 2, -4, 0, true));
                pipeline.addLast(new CastelProtocolEncoder(CastelProtocol.this));
                pipeline.addLast(new CastelProtocolDecoder(CastelProtocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new CastelProtocolEncoder(CastelProtocol.this));
                pipeline.addLast(new CastelProtocolDecoder(CastelProtocol.this));
            }
        });
    }

}
