
package org.telescope.protocol;

import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import org.telescope.config.Keys;
import org.telescope.server.BaseProtocol;
import org.telescope.server.Context;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class Mta6Protocol extends BaseProtocol {

    public Mta6Protocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new HttpResponseEncoder());
                pipeline.addLast(new HttpRequestDecoder());
                pipeline.addLast(new HttpObjectAggregator(65535));
                pipeline.addLast(new Mta6ProtocolDecoder(
                        Mta6Protocol.this, !Context.getConfig().getBoolean(Keys.PROTOCOL_CAN.withPrefix(getName()))));
            }
        });
    }

}
