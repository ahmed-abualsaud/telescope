
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class EsealProtocolEncoder extends StringProtocolEncoder {

    public EsealProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return formatCommand(
                        command, "##S,eSeal,%s,256,3.0.8,%s,E##", Command.KEY_UNIQUE_ID, Command.KEY_DATA);
            case Command.TYPE_ALARM_ARM:
                return formatCommand(
                        command, "##S,eSeal,%s,256,3.0.8,RC-Power Control,Power OFF,E##", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ALARM_DISARM:
                return formatCommand(
                        command, "##S,eSeal,%s,256,3.0.8,RC-Unlock,E##", Command.KEY_UNIQUE_ID);
            default:
                return null;
        }
    }

}
