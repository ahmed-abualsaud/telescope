
package org.telescope.protocol;

import org.telescope.javel.framework.helper.Checksum;
import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class LaipacProtocolEncoder extends StringProtocolEncoder {

    public LaipacProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected String formatCommand(Command command, String format, String... keys) {
        String sentence = super.formatCommand(command, "$" + format, keys);
        sentence += Checksum.nmea(sentence.substring(1)) + "\r\n";
        return sentence;
    }

    @Override
    protected Object encodeCommand(Command command) {

        initDevicePassword(command, LaipacProtocolDecoder.DEFAULT_DEVICE_PASSWORD);

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return formatCommand(command, "%s",
                    Command.KEY_DATA);
            case Command.TYPE_POSITION_SINGLE:
                return formatCommand(command, "AVREQ,%s,1",
                    Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_REBOOT_DEVICE:
                return formatCommand(command, "AVRESET,%s,%s",
                    Command.KEY_UNIQUE_ID, Command.KEY_DEVICE_PASSWORD);
            default:
                return null;
        }

    }

}
