
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.CharacterDelimiterFrameDecoder;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class ArknavX8Protocol extends BaseProtocol {

    public ArknavX8Protocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new CharacterDelimiterFrameDecoder(1024, ';'));
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new ArknavX8ProtocolDecoder(ArknavX8Protocol.this));
            }
        });
    }

}
