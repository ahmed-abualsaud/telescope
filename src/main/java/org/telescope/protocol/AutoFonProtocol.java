
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class AutoFonProtocol extends BaseProtocol {

    public AutoFonProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new AutoFonFrameDecoder());
                pipeline.addLast(new AutoFonProtocolDecoder(AutoFonProtocol.this));
            }
        });
    }

}
