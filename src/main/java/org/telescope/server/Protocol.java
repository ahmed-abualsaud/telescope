
package org.telescope.server;

import io.netty.channel.Channel;
import org.telescope.model.Command;

import java.net.SocketAddress;
import java.util.Collection;

public interface Protocol {

    String getName();

    Collection<TrackerServer> getServerList();

    Collection<String> getSupportedDataCommands();

    void sendDataCommand(Channel channel, SocketAddress remoteAddress, Command command);

    Collection<String> getSupportedTextCommands();

    void sendTextCommand(String destAddress, Command command) throws Exception;

}
