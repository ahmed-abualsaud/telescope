
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.UnitsConverter;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.Protocol;

import java.net.SocketAddress;
import java.util.Date;

public class Mavlink2ProtocolDecoder extends BaseProtocolDecoder {

    public Mavlink2ProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        if (buf.readUnsignedByte() != 0xFD) {
            return null;
        }

        buf.readUnsignedByte(); // length
        buf.readUnsignedByte(); // incompatibility flags
        buf.readUnsignedByte(); // compatibility flags
        buf.readUnsignedByte(); // index

        int senderSystemId = buf.readUnsignedByte();
        buf.readUnsignedByte(); // component id
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, Integer.toString(senderSystemId));
        if (deviceSession == null) {
            return null;
        }

        int type = buf.readUnsignedMediumLE();
        if (type == 33) {

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            position.set("timeBoot", buf.readUnsignedIntLE()); // time since system boot

            position.setValid(true);
            position.setTime(new Date());
            position.setLatitude(buf.readIntLE() / 10000000.0);
            position.setLongitude(buf.readIntLE() / 10000000.0);
            position.setAltitude(buf.readIntLE() / 1000.0);
            position.set("relativeAltitude", buf.readIntLE() / 1000.0);

            int groundSpeedX = buf.readShortLE();
            int groundSpeedY = buf.readShortLE();
            buf.readShortLE(); // ground speed z
            double speed = Math.sqrt(Math.pow(groundSpeedX, 2) + Math.pow(groundSpeedY, 2));
            position.setSpeed(UnitsConverter.knotsFromCps(speed));

            position.setCourse(buf.readUnsignedShortLE() / 100.0);

            return position;

        }

        return null;
    }

}
