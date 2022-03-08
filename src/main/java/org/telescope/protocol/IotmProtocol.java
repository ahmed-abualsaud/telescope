
package org.telescope.protocol;

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

public class IotmProtocol extends BaseProtocol {

    public IotmProtocol() {
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(MqttEncoder.INSTANCE);
                pipeline.addLast(new MqttDecoder());
                pipeline.addLast(new IotmProtocolDecoder(IotmProtocol.this));
            }
        });
    }

}
