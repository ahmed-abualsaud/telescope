
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.CharacterDelimiterFrameDecoder;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.string.StringEncoder;

public class UproProtocol extends BaseProtocol {

    public UproProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new CharacterDelimiterFrameDecoder(1024, '#'));
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new UproProtocolDecoder(UproProtocol.this));
            }
        });
    }

}
