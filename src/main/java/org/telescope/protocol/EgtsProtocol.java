
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class EgtsProtocol extends BaseProtocol {

    public EgtsProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new EgtsFrameDecoder());
                pipeline.addLast(new EgtsProtocolDecoder(EgtsProtocol.this));
            }
        });
    }

}
