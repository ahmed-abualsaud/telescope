
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.telescope.javel.framework.helper.DataConverter;
import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Context;
import org.telescope.server.Protocol;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HuabaoProtocolEncoder extends BaseProtocolEncoder {

    public HuabaoProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        boolean alternative = Context.getIdentityManager().lookupAttributeBoolean(
                command.getDeviceId(), getProtocolName() + ".alternative", false, false, true);

        ByteBuf id = Unpooled.wrappedBuffer(
                DataConverter.parseHex(getUniqueId(command.getDeviceId())));
        try {
            ByteBuf data = Unpooled.buffer();
            byte[] time = DataConverter.parseHex(new SimpleDateFormat("yyMMddHHmmss").format(new Date()));

            switch (command.getType()) {
                case Command.TYPE_ENGINE_STOP:
                    if (alternative) {
                        data.writeByte(0x01);
                        data.writeBytes(time);
                        return HuabaoProtocolDecoder.formatMessage(
                                HuabaoProtocolDecoder.MSG_OIL_CONTROL, id, false, data);
                    } else {
                        data.writeByte(0xf0);
                        return HuabaoProtocolDecoder.formatMessage(
                                HuabaoProtocolDecoder.MSG_TERMINAL_CONTROL, id, false, data);
                    }
                case Command.TYPE_ENGINE_RESUME:
                    if (alternative) {
                        data.writeByte(0x00);
                        data.writeBytes(time);
                        return HuabaoProtocolDecoder.formatMessage(
                                HuabaoProtocolDecoder.MSG_OIL_CONTROL, id, false, data);
                    } else {
                        data.writeByte(0xf1);
                        return HuabaoProtocolDecoder.formatMessage(
                                HuabaoProtocolDecoder.MSG_TERMINAL_CONTROL, id, false, data);
                    }
                default:
                    return null;
            }
        } finally {
            id.release();
        }
    }

}
