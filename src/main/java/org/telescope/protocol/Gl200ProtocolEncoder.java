
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class Gl200ProtocolEncoder extends StringProtocolEncoder {

    public Gl200ProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        initDevicePassword(command, "");

        switch (command.getType()) {
            case Command.TYPE_POSITION_SINGLE:
                return formatCommand(command, "AT+GTRTO=%s,1,,,,,,FFFF$", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(command, "AT+GTOUT=%s,1,,,0,0,0,0,0,0,0,,,,,,,FFFF$",
                        Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(command, "AT+GTOUT=%s,0,,,0,0,0,0,0,0,0,,,,,,,FFFF$",
                        Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_IDENTIFICATION:
                return formatCommand(command, "AT+GTRTO=%s,8,,,,,,FFFF$", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_REBOOT_DEVICE:
                return formatCommand(command, "AT+GTRTO=%s,3,,,,,,FFFF$", Command.KEY_DEVICE_PASSWORD);
            default:
                return null;
        }
    }

}
