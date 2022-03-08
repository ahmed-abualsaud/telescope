
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class XirgoProtocolEncoder extends StringProtocolEncoder {

    public XirgoProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_OUTPUT_CONTROL:
                return String.format("+XT:7005,%d,1", command.getInteger(Command.KEY_DATA) + 1);
            default:
                return null;
        }
    }

}
