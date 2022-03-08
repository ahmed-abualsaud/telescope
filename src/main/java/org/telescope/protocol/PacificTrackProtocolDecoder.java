
package org.telescope.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;

import org.telescope.javel.framework.helper.BitUtil;
import org.telescope.javel.framework.helper.DateBuilder;
import org.telescope.javel.framework.helper.UnitsConverter;
import org.telescope.model.Position;
import org.telescope.server.BaseProtocolDecoder;
import org.telescope.server.DeviceSession;
import org.telescope.server.Protocol;

import java.net.SocketAddress;

public class PacificTrackProtocolDecoder extends BaseProtocolDecoder {

    public PacificTrackProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    public static int readBitExt(ByteBuf buf) {
        int result = 0;
        while (buf.isReadable()) {
            int b = buf.readUnsignedByte();
            result <<= 7;
            result += BitUtil.to(b, 7);
            if (BitUtil.check(b, 7)) {
                break;
            }
        }
        return result;
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.readByte(); // frame start
        readBitExt(buf); // frame control
        readBitExt(buf); // frame length

        DeviceSession deviceSession = null;
        Position position = new Position(getProtocolName());

        while (buf.isReadable()) {

            int segmentId = readBitExt(buf);
            int segmentEnd = readBitExt(buf) + buf.readerIndex();

            switch (segmentId) {
                case 0x01:
                    position.set(Position.KEY_EVENT, readBitExt(buf));
                    break;
                case 0x10:
                    position.setValid(BitUtil.check(buf.readUnsignedByte(), 4));
                    int date = buf.readUnsignedByte();
                    DateBuilder dateBuilder = new DateBuilder()
                            .setDate(2020 + BitUtil.from(date, 4), BitUtil.to(date, 4), buf.readUnsignedByte())
                            .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
                    position.setTime(dateBuilder.getDate());
                    position.setLatitude(buf.readUnsignedInt() / 1000000.0 - 90.0);
                    position.setLongitude(buf.readUnsignedInt() / 1000000.0 - 180.0);
                    int speedAndCourse = buf.readUnsignedMedium();
                    position.setCourse(BitUtil.from(speedAndCourse, 12));
                    position.setSpeed(UnitsConverter.knotsFromKph(BitUtil.to(speedAndCourse, 12) * 0.1));
                    position.set(Position.KEY_INDEX, buf.readUnsignedShort());
                    break;
                case 0x20:
                    int voltage = buf.readUnsignedMedium();
                    position.set(Position.KEY_BATTERY, BitUtil.between(voltage, 0, 12) * 0.01);
                    position.set(Position.KEY_POWER, BitUtil.between(voltage, 12, 24) * 0.01);
                    break;
                case 0x92:
                    while (buf.readerIndex() < segmentEnd) {
                        int field = buf.readUnsignedByte();
                        int fieldPrefix = BitUtil.from(field, 5);
                        if (fieldPrefix < 0b100) {
                            switch (BitUtil.between(field, 2, 5)) {
                                case 0b000:
                                    position.set("bus", BitUtil.to(field, 2));
                                    break;
                                case 0b001:
                                    position.set("currentGear", BitUtil.to(field, 2));
                                    break;
                                default:
                                    break;
                            }
                        } else if (fieldPrefix < 0b101) {
                            switch (BitUtil.to(field, 5)) {
                                case 0b00000:
                                    position.set(Position.KEY_OBD_SPEED, buf.readUnsignedByte());
                                    break;
                                case 0b00001:
                                    position.set(Position.KEY_RPM, buf.readUnsignedByte() * 32);
                                    break;
                                case 0b00011:
                                    position.set("oilPressure", buf.readUnsignedByte() * 4);
                                    break;
                                case 0b00100:
                                    position.set("oilLevel", buf.readUnsignedByte() * 0.4);
                                    break;
                                case 0b00101:
                                    position.set("oilTemp", buf.readUnsignedByte() - 40);
                                    break;
                                case 0b00110:
                                    position.set("coolantLevel", buf.readUnsignedByte() * 0.4);
                                    break;
                                case 0b00111:
                                    position.set(Position.KEY_COOLANT_TEMP, buf.readUnsignedByte() - 40);
                                    break;
                                case 0b01000:
                                    position.set(Position.KEY_FUEL_LEVEL, buf.readUnsignedByte() * 0.4);
                                    break;
                                case 0b01001:
                                    position.set("defLevel", buf.readUnsignedByte() * 0.4);
                                    break;
                                case 0b01010:
                                    position.set(Position.KEY_ENGINE_LOAD, buf.readUnsignedByte());
                                    break;
                                case 0b01011:
                                    position.set("barometer", buf.readUnsignedByte() * 0.5);
                                    break;
                                case 0b01100:
                                    position.set("intakeManifoldTemp", buf.readUnsignedByte() - 40);
                                    break;
                                case 0b01101:
                                    position.set("fuelTankTemp", buf.readUnsignedByte() - 40);
                                    break;
                                case 0b01110:
                                    position.set("intercoolerTemp", buf.readUnsignedByte() - 40);
                                    break;
                                case 0b01111:
                                    position.set("turboOilTemp", buf.readUnsignedByte() - 40);
                                    break;
                                case 0b10000:
                                    position.set("transOilTemp", buf.readUnsignedByte() - 40);
                                    break;
                                default:
                                    buf.readUnsignedByte();
                                    break;
                            }
                        } else if (fieldPrefix < 0b110) {
                            switch (BitUtil.to(field, 5)) {
                                case 0b00010:
                                    position.set(Position.KEY_FUEL_CONSUMPTION, buf.readUnsignedShort() / 512.0);
                                    break;
                                case 0b00011:
                                    position.set(Position.PREFIX_TEMP + 1, buf.readUnsignedShort() * 0.03125 - 273);
                                    break;
                                default:
                                    buf.readUnsignedShort();
                                    break;
                            }
                        }  else if (fieldPrefix < 0b111) {
                            switch (BitUtil.to(field, 5)) {
                                case 0b00000:
                                    position.set(Position.KEY_ODOMETER, buf.readUnsignedInt() * 100);
                                    break;
                                case 0b00001:
                                    position.set(Position.KEY_HOURS, buf.readUnsignedInt() * 180);
                                    break;
                                case 0b00010:
                                    position.set("idleHours", buf.readUnsignedInt() * 180);
                                    break;
                                case 0b00100:
                                    position.set(Position.KEY_FUEL_USED, buf.readUnsignedInt() * 0.5);
                                    break;
                                case 0b00101:
                                    position.set("fuelUsedIdle", buf.readUnsignedInt() * 0.5);
                                    break;
                                default:
                                    buf.readUnsignedInt();
                                    break;
                            }
                        } else {
                            buf.skipBytes(buf.readUnsignedByte());
                        }
                    }
                    break;
                case 0x100:
                    String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(0, 15);
                    deviceSession = getDeviceSession(channel, remoteAddress, imei);
                    break;
                default:
                    buf.readerIndex(segmentEnd);
                    break;
            }
        }

        if (deviceSession == null) {
            deviceSession = getDeviceSession(channel, remoteAddress);
        }

        if (deviceSession != null) {
            position.setDeviceId(deviceSession.getDeviceId());
            if (position.getFixTime() == null) {
                getLastLocation(position, null);
            }
            return position;
        } else {
            return null;
        }
    }

}
