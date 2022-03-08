
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class PortmanProtocolEncoder extends StringProtocolEncoder {

    public PortmanProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(command, "&&%s,XA5\r\n", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(command, "&&%s,XA6\r\n", Command.KEY_UNIQUE_ID);
            default:
                return null;
        }
    }

}
