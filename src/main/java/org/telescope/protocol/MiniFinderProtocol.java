
package org.telescope.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.CharacterDelimiterFrameDecoder;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class MiniFinderProtocol extends BaseProtocol {

    public MiniFinderProtocol() {
        setSupportedDataCommands(
                Command.TYPE_SET_TIMEZONE,
                Command.TYPE_VOICE_MONITORING,
                Command.TYPE_ALARM_SPEED,
                Command.TYPE_ALARM_GEOFENCE,
                Command.TYPE_ALARM_VIBRATION,
                Command.TYPE_SET_AGPS,
                Command.TYPE_ALARM_FALL,
                Command.TYPE_MODE_POWER_SAVING,
                Command.TYPE_MODE_DEEP_SLEEP,
                Command.TYPE_SOS_NUMBER,
                Command.TYPE_SET_INDICATOR);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new CharacterDelimiterFrameDecoder(1024, ';'));
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new MiniFinderProtocolEncoder(MiniFinderProtocol.this));
                pipeline.addLast(new MiniFinderProtocolDecoder(MiniFinderProtocol.this));
            }
        });
    }

}
