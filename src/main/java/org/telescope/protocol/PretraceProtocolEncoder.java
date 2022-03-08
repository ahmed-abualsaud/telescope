
package org.telescope.protocol;

import org.telescope.javel.framework.helper.Checksum;
import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Context;
import org.telescope.server.Protocol;

public class PretraceProtocolEncoder extends BaseProtocolEncoder {

    public PretraceProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    private String formatCommand(String uniqueId, String data) {
        String content = uniqueId + data;
        return String.format("(%s^%02X)", content, Checksum.xor(content));
    }

    @Override
    protected Object encodeCommand(Command command) {

        String uniqueId = Context.getIdentityManager().getById(command.getDeviceId()).getUniqueId();

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return formatCommand(uniqueId, command.getString(Command.KEY_DATA));
            case Command.TYPE_POSITION_PERIODIC:
                return formatCommand(
                        uniqueId, String.format("D221%1$d,%1$d,,", command.getInteger(Command.KEY_FREQUENCY)));
            default:
                return null;
        }
    }

}
