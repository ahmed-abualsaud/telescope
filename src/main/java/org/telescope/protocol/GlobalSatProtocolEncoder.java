
package org.telescope.protocol;

import org.telescope.javel.framework.helper.Checksum;
import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class GlobalSatProtocolEncoder extends StringProtocolEncoder {

    public GlobalSatProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        String formattedCommand = null;

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                formattedCommand = formatCommand(
                        command, "GSC,%s,%s", Command.KEY_UNIQUE_ID, Command.KEY_DATA);
                break;
            case Command.TYPE_ALARM_DISMISS:
                formattedCommand = formatCommand(
                        command, "GSC,%s,Na", Command.KEY_UNIQUE_ID);
                break;
            case Command.TYPE_OUTPUT_CONTROL:
                formattedCommand = formatCommand(
                        command, "GSC,%s,Lo(%s,%s)", Command.KEY_UNIQUE_ID, Command.KEY_INDEX, Command.KEY_DATA);
                break;
            default:
                break;
        }

        if (formattedCommand != null) {
            return formattedCommand + Checksum.nmea(formattedCommand) + '!';
        } else {
            return null;
        }
    }

}
