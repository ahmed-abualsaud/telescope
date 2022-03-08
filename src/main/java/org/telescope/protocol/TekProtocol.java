
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class TekProtocol extends BaseProtocol {

    public TekProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new TekFrameDecoder());
                pipeline.addLast(new TekProtocolDecoder(TekProtocol.this));
            }
        });
    }

}
