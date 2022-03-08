
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class FlexibleReportProtocol extends BaseProtocol {

    public FlexibleReportProtocol() {
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new FlexibleReportProtocolDecoder(FlexibleReportProtocol.this));
            }
        });
    }

}
