
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class FlespiProtocol extends BaseProtocol {

    public FlespiProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new HttpResponseEncoder());
                pipeline.addLast(new HttpRequestDecoder(4096, 8192, 128 * 1024));
                pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
                pipeline.addLast(new FlespiProtocolDecoder(FlespiProtocol.this));
            }
        });
    }
}
