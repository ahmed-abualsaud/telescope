
package org.telescope.server;

import io.netty.channel.ChannelHandler;

public interface PipelineBuilder {

    void addLast(ChannelHandler handler);

}
