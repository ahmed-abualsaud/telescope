
package org.telescope.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.model.Command;

public abstract class BaseProtocolEncoder extends ChannelOutboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseProtocolEncoder.class);

    private static final String PROTOCOL_UNKNOWN = "unknown";

    private final Protocol protocol;

    public BaseProtocolEncoder(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getProtocolName() {
        return protocol != null ? protocol.getName() : PROTOCOL_UNKNOWN;
    }

    protected String getUniqueId(long deviceId) {
        return Context.getIdentityManager().getById(deviceId).getUniqueId();
    }

    protected void initDevicePassword(Command command, String defaultPassword) {
        if (!command.getAttributes().containsKey(Command.KEY_DEVICE_PASSWORD)) {
            String password = Context.getIdentityManager()
                .getDevicePassword(command.getDeviceId(), getProtocolName(), defaultPassword);
            command.set(Command.KEY_DEVICE_PASSWORD, password);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        NetworkMessage networkMessage = (NetworkMessage) msg;

        if (networkMessage.getMessage() instanceof Command) {

            Command command = (Command) networkMessage.getMessage();
            Object encodedCommand = encodeCommand(ctx.channel(), command);

            StringBuilder s = new StringBuilder();
            s.append("[").append(ctx.channel().id().asShortText()).append("] ");
            s.append("id: ").append(getUniqueId(command.getDeviceId())).append(", ");
            s.append("command type: ").append(command.getType()).append(" ");
            if (encodedCommand != null) {
                s.append("sent");
            } else {
                s.append("not sent");
            }
            LOGGER.info(s.toString());

            ctx.write(new NetworkMessage(encodedCommand, networkMessage.getRemoteAddress()), promise);

        } else {

            super.write(ctx, msg, promise);

        }
    }

    protected Object encodeCommand(Channel channel, Command command) {
        return encodeCommand(command);
    }

    protected Object encodeCommand(Command command) {
        return null;
    }

}
