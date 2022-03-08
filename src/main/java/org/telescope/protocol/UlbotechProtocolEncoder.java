
package org.telescope.protocol;

import io.netty.buffer.Unpooled;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Protocol;

import java.nio.charset.StandardCharsets;

public class UlbotechProtocolEncoder extends BaseProtocolEncoder {

    public UlbotechProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {
        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return Unpooled.copiedBuffer(
                        "*TS01," + command.getString(Command.KEY_DATA) + "#", StandardCharsets.US_ASCII);
            default:
                return null;
        }
    }

}
