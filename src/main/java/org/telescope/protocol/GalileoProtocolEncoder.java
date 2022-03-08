
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.telescope.javel.framework.helper.Checksum;
import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Protocol;

import java.nio.charset.StandardCharsets;

public class GalileoProtocolEncoder extends BaseProtocolEncoder {

    public GalileoProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    private ByteBuf encodeText(String uniqueId, String text) {

        ByteBuf buf = Unpooled.buffer(256);

        buf.writeByte(0x01);
        buf.writeShortLE(uniqueId.length() + text.length() + 11);

        buf.writeByte(0x03); // imei tag
        buf.writeBytes(uniqueId.getBytes(StandardCharsets.US_ASCII));

        buf.writeByte(0x04); // device id tag
        buf.writeShortLE(0); // not needed if imei provided

        buf.writeByte(0xE0); // index tag
        buf.writeIntLE(0); // index

        buf.writeByte(0xE1); // command text tag
        buf.writeByte(text.length());
        buf.writeBytes(text.getBytes(StandardCharsets.US_ASCII));

        buf.writeShortLE(Checksum.crc16(Checksum.CRC16_MODBUS, buf.nioBuffer(0, buf.writerIndex())));

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return encodeText(getUniqueId(command.getDeviceId()), command.getString(Command.KEY_DATA));
            case Command.TYPE_OUTPUT_CONTROL:
                return encodeText(getUniqueId(command.getDeviceId()),
                        "Out " + command.getInteger(Command.KEY_INDEX) + "," + command.getString(Command.KEY_DATA));
            default:
                return null;
        }
    }

}