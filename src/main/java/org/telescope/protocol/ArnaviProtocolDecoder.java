
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.net.SocketAddress;

import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.Protocol;

public class ArnaviProtocolDecoder extends BaseProtocolDecoder {

    private final ArnaviTextProtocolDecoder textProtocolDecoder;
    private final ArnaviBinaryProtocolDecoder binaryProtocolDecoder;

    public ArnaviProtocolDecoder(Protocol protocol) {
        super(protocol);
        textProtocolDecoder = new ArnaviTextProtocolDecoder(protocol);
        binaryProtocolDecoder = new ArnaviBinaryProtocolDecoder(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        if (buf.getByte(buf.readerIndex()) == '$') {
            return textProtocolDecoder.decode(channel, remoteAddress, msg);
        } else {
            return binaryProtocolDecoder.decode(channel, remoteAddress, msg);
        }
    }

}
