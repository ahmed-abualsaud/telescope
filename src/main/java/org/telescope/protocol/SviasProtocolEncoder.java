
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class SviasProtocolEncoder extends StringProtocolEncoder {

    public SviasProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {
        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return formatCommand(command, "%s", Command.KEY_DATA);
            case Command.TYPE_POSITION_SINGLE:
                return formatCommand(command, "AT+STR=1*");
            case Command.TYPE_SET_ODOMETER:
                return formatCommand(command, "AT+ODT=%s*", Command.KEY_DATA);
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(command, "AT+OUT=1,1*");
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(command, "AT+OUT=1,0*");
            case Command.TYPE_ALARM_ARM:
                return formatCommand(command, "AT+OUT=2,1*");
            case Command.TYPE_ALARM_DISARM:
                return formatCommand(command, "AT+OUT=2,0*");
            case Command.TYPE_ALARM_REMOVE:
                return formatCommand(command, "AT+PNC=600*");
            default:
                return null;
        }
    }

}
