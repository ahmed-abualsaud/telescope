
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class MeiligaoProtocol extends BaseProtocol {

    public MeiligaoProtocol() {
        setSupportedDataCommands(
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_POSITION_PERIODIC,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_ALARM_GEOFENCE,
                Command.TYPE_SET_TIMEZONE,
                Command.TYPE_REQUEST_PHOTO,
                Command.TYPE_REBOOT_DEVICE);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new MeiligaoFrameDecoder());
                pipeline.addLast(new MeiligaoProtocolEncoder(MeiligaoProtocol.this));
                pipeline.addLast(new MeiligaoProtocolDecoder(MeiligaoProtocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new MeiligaoProtocolEncoder(MeiligaoProtocol.this));
                pipeline.addLast(new MeiligaoProtocolDecoder(MeiligaoProtocol.this));
            }
        });
    }

}
