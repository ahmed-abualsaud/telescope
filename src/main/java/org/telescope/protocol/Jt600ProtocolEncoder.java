
package org.telescope.protocol;

import java.util.TimeZone;

import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class Jt600ProtocolEncoder extends StringProtocolEncoder {

    public Jt600ProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_ENGINE_STOP:
                return "(S07,0)";
            case Command.TYPE_ENGINE_RESUME:
                return "(S07,1)";
            case Command.TYPE_SET_TIMEZONE:
                int offset = TimeZone.getTimeZone(command.getString(Command.KEY_TIMEZONE)).getRawOffset() / 60000;
                return "(S09,1," + offset + ")";
            case Command.TYPE_REBOOT_DEVICE:
                return "(S17)";
            default:
                return null;
        }
    }

}
