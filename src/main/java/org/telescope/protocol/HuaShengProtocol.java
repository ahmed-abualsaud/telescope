
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class HuaShengProtocol extends BaseProtocol {

    public HuaShengProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new HuaShengFrameDecoder());
                pipeline.addLast(new HuaShengProtocolDecoder(HuaShengProtocol.this));
            }
        });
    }

}
