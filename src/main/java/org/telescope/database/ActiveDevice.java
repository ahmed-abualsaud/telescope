
package org.telescope.database;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequestDecoder;

import org.telescope.model.Command;
import org.telescope.server.BasePipelineFactory;
import org.telescope.server.Protocol;

import java.net.SocketAddress;

public class ActiveDevice {

    private final long deviceId;
    private final Protocol protocol;
    private final Channel channel;
    private final SocketAddress remoteAddress;
    private final boolean supportsLiveCommands;

    public ActiveDevice(long deviceId, Protocol protocol, Channel channel, SocketAddress remoteAddress) {
        this.deviceId = deviceId;
        this.protocol = protocol;
        this.channel = channel;
        this.remoteAddress = remoteAddress;
        supportsLiveCommands = BasePipelineFactory.getHandler(channel.pipeline(), HttpRequestDecoder.class) == null;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public boolean supportsLiveCommands() {
        return supportsLiveCommands;
    }

    public void sendCommand(Command command) {
        protocol.sendDataCommand(channel, remoteAddress, command);
    }

}
