
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.Checksum;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.NetworkMessage;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class NavtelecomProtocolDecoder extends BaseProtocolDecoder {

    public NavtelecomProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private void sendResponse(
            Channel channel, SocketAddress remoteAddress, int receiver, int sender, ByteBuf content) {
        if (channel != null) {
            ByteBuf response = Unpooled.buffer();
            response.writeCharSequence("@NTC", StandardCharsets.US_ASCII);
            response.writeIntLE(sender);
            response.writeIntLE(receiver);
            response.writeShortLE(content.readableBytes());
            response.writeByte(Checksum.xor(content.nioBuffer()));
            response.writeByte(Checksum.xor(response.nioBuffer()));
            response.writeBytes(content);
            content.release();

            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.skipBytes(4); // preamble
        int receiver = buf.readIntLE();
        int sender = buf.readIntLE();
        int length = buf.readUnsignedShortLE();
        buf.readUnsignedByte(); // data checksum
        buf.readUnsignedByte(); // header checksum

        String type = buf.toString(buf.readerIndex(), 6, StandardCharsets.US_ASCII);

        if (type.startsWith("*>S")) {

            String sentence = buf.readCharSequence(length, StandardCharsets.US_ASCII).toString();
            getDeviceSession(channel, remoteAddress, sentence.substring(4));

            ByteBuf payload = Unpooled.copiedBuffer("*<S", StandardCharsets.US_ASCII);

            sendResponse(channel, remoteAddress, receiver, sender, payload);

        } else if (type.startsWith("*>FLEX")) {

            buf.skipBytes(6);

            ByteBuf payload = Unpooled.buffer();
            payload.writeByte(buf.readUnsignedByte()); // protocol
            payload.writeByte(buf.readUnsignedByte()); // protocol version
            payload.writeByte(buf.readUnsignedByte()); // struct version

            sendResponse(channel, remoteAddress, receiver, sender, payload);

        }

        return null;
    }

}
