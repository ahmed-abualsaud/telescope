
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.CharacterDelimiterFrameDecoder;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.string.StringDecoder;

public class Ivt401Protocol extends BaseProtocol {

    public Ivt401Protocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new CharacterDelimiterFrameDecoder(1024, ';'));
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new Ivt401ProtocolDecoder(Ivt401Protocol.this));
            }
        });
    }

}
