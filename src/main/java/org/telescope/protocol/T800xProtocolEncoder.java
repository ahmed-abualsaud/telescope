
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.DataConverter;
import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Protocol;

import java.nio.charset.StandardCharsets;

public class T800xProtocolEncoder extends BaseProtocolEncoder {

    public static final int MODE_SETTING = 0x01;
    public static final int MODE_BROADCAST = 0x02;
    public static final int MODE_FORWARD = 0x03;

    public T800xProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    private ByteBuf encodeContent(Command command, short header, String content) {

        ByteBuf buf = Unpooled.buffer();

        buf.writeShort(header);
        buf.writeByte(T800xProtocolDecoder.MSG_COMMAND);
        buf.writeShort(7 + 8 + 1 + content.length());
        buf.writeShort(1); // serial number
        buf.writeBytes(DataConverter.parseHex("0" + getUniqueId(command.getDeviceId())));
        buf.writeByte(MODE_SETTING);
        buf.writeBytes(content.getBytes(StandardCharsets.US_ASCII));

        return buf;
    }

    @Override
    protected Object encodeCommand(Channel channel, Command command) {

        short header = T800xProtocolDecoder.DEFAULT_HEADER;
        if (channel != null) {
            header = channel.pipeline().get(T800xProtocolDecoder.class).getHeader();
        }

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return encodeContent(command, header, command.getString(Command.KEY_DATA));
            default:
                return null;
        }
    }

}
