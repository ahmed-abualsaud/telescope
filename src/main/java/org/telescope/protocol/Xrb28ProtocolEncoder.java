
package org.telescope.protocol;

import io.netty.channel.Channel;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocolEncoder;
import org.telescope.server.Protocol;

public class Xrb28ProtocolEncoder extends BaseProtocolEncoder {

    public Xrb28ProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    private String formatCommand(Command command, String content) {
        return String.format("\u00ff\u00ff*SCOS,OM,%s,%s#\n", getUniqueId(command.getDeviceId()), content);
    }

    @Override
    protected Object encodeCommand(Channel channel, Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return formatCommand(command, command.getString(Command.KEY_DATA));
            case Command.TYPE_POSITION_SINGLE:
                return formatCommand(command, "D0");
            case Command.TYPE_POSITION_PERIODIC:
                return formatCommand(command, "D1," + command.getInteger(Command.KEY_FREQUENCY));
            case Command.TYPE_ENGINE_STOP:
            case Command.TYPE_ALARM_DISARM:
                if (channel != null) {
                    Xrb28ProtocolDecoder decoder = channel.pipeline().get(Xrb28ProtocolDecoder.class);
                    if (decoder != null) {
                        decoder.setPendingCommand(command.getType());
                    }
                }
                return formatCommand(command, "R0,0,20,1234," + System.currentTimeMillis() / 1000);
            default:
                return null;
        }
    }

}
