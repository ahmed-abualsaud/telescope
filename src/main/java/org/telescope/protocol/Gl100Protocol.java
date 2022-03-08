
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.CharacterDelimiterFrameDecoder;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Gl100Protocol extends BaseProtocol {

    public Gl100Protocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new CharacterDelimiterFrameDecoder(1024, '\0'));
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new Gl100ProtocolDecoder(Gl100Protocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new Gl100ProtocolDecoder(Gl100Protocol.this));
            }
        });
    }

}
