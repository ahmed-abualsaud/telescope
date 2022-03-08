
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class GranitProtocolSmsEncoder extends StringProtocolEncoder {

    public GranitProtocolSmsEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected String encodeCommand(Command command) {
        switch (command.getType()) {
        case Command.TYPE_REBOOT_DEVICE:
            return "BB+RESET";
        case Command.TYPE_POSITION_PERIODIC:
            return formatCommand(command, "BB+BBMD=%s", Command.KEY_FREQUENCY);
        default:
            return null;
        }
    }

}
