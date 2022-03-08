
package org.telescope.protocol;

import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.Checksum;
import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class StartekProtocolEncoder extends StringProtocolEncoder {

    public StartekProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected String formatCommand(Command command, String format, String... keys) {
        String uniqueId = getUniqueId(command.getDeviceId());
        String payload = super.formatCommand(command, format, keys);
        int length = 1 + uniqueId.length() + 1 + payload.length();
        String sentence = "$$:" + length + "," + uniqueId + "," + payload;
        return sentence + Checksum.sum(sentence) + "\r\n";
    }

    @Override
    protected Object encodeCommand(Channel channel, Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return formatCommand(command, "%s", Command.KEY_DATA);
            case Command.TYPE_OUTPUT_CONTROL:
                return formatCommand(command, "900,%s,%s", Command.KEY_INDEX, Command.KEY_DATA);
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(command, "900,1,1");
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(command, "900,1,0");
            default:
                return null;
        }
    }

}
