
package org.telescope.protocol;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Protocol;

public class GranitProtocolEncoder extends BaseProtocolEncoder {

    public GranitProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    private ByteBuf encodeCommand(String commandString) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(commandString.getBytes(StandardCharsets.US_ASCII));
        GranitProtocolDecoder.appendChecksum(buffer, commandString.length());
        return buffer;
    }

    @Override
    protected Object encodeCommand(Command command) {
        switch (command.getType()) {
            case Command.TYPE_IDENTIFICATION:
                return encodeCommand("BB+IDNT");
            case Command.TYPE_REBOOT_DEVICE:
                return encodeCommand("BB+RESET");
            case Command.TYPE_POSITION_SINGLE:
                return encodeCommand("BB+RRCD");
            default:
                return null;
        }
    }

}
