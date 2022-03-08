
package org.telescope.protocol;

import io.netty.buffer.Unpooled;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Protocol;

import java.nio.charset.StandardCharsets;

public class AtrackProtocolEncoder extends BaseProtocolEncoder {

    public AtrackProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return Unpooled.copiedBuffer(
                        command.getString(Command.KEY_DATA) + "\r\n", StandardCharsets.US_ASCII);
            default:
                return null;
        }
    }

}
