
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class EasyTrackProtocolEncoder extends StringProtocolEncoder {

    public EasyTrackProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(command, "*ET,%s,FD,Y1#", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(command, "*ET,%s,FD,Y2#", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ALARM_ARM:
                return formatCommand(command, "*ET,%s,FD,F1#", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ALARM_DISARM:
                return formatCommand(command, "*ET,%s,FD,F2#", Command.KEY_UNIQUE_ID);
            default:
                return null;
        }
    }

}
