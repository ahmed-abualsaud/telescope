
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class TramigoProtocol extends BaseProtocol {

    public TramigoProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new TramigoFrameDecoder());
                pipeline.addLast(new TramigoProtocolDecoder(TramigoProtocol.this));
            }
        });
    }

}
