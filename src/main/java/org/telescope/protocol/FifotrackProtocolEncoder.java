
package org.telescope.protocol;

import org.telescope.javel.framework.helper.Checksum;
import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class FifotrackProtocolEncoder extends StringProtocolEncoder {

    public FifotrackProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    private Object formatCommand(Command command, String content) {
        String uniqueId = getUniqueId(command.getDeviceId());
        int length = 1 + uniqueId.length() + 3 + content.length();
        String result = String.format("##%02d,%s,1,%s*", length, uniqueId, content);
        result += Checksum.sum(result) + "\r\n";
        return result;
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return formatCommand(command, command.getString(Command.KEY_DATA));
            case Command.TYPE_REQUEST_PHOTO:
                return formatCommand(command, "D05,3");
            default:
                return null;
        }
    }

}
