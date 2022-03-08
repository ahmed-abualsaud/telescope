
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class CarcellProtocolEncoder extends StringProtocolEncoder {

    public CarcellProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(command, "$SRVCMD,%s,BA#\r\n", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(command, "$SRVCMD,%s,BD#\r\n", Command.KEY_UNIQUE_ID);
            default:
                return null;
        }
    }

}
