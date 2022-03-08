
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class WatchProtocol extends BaseProtocol {

    public WatchProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_POSITION_PERIODIC,
                Command.TYPE_SOS_NUMBER,
                Command.TYPE_ALARM_SOS,
                Command.TYPE_ALARM_BATTERY,
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_POWER_OFF,
                Command.TYPE_ALARM_REMOVE,
                Command.TYPE_SILENCE_TIME,
                Command.TYPE_ALARM_CLOCK,
                Command.TYPE_SET_PHONEBOOK,
                Command.TYPE_MESSAGE,
                Command.TYPE_VOICE_MESSAGE,
                Command.TYPE_SET_TIMEZONE,
                Command.TYPE_SET_INDICATOR);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new WatchFrameDecoder());
                pipeline.addLast(new WatchProtocolEncoder(WatchProtocol.this));
                pipeline.addLast(new WatchProtocolDecoder(WatchProtocol.this));
            }
        });
    }

}
