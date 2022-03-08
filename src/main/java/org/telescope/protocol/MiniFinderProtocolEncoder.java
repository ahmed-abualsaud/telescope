
package org.telescope.protocol;

import java.util.TimeZone;

import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class MiniFinderProtocolEncoder extends StringProtocolEncoder implements StringProtocolEncoder.ValueFormatter {

    public MiniFinderProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    public String formatValue(String key, Object value) {
        switch (key) {
            case Command.KEY_ENABLE:
                return (Boolean) value ? "1" : "0";
            case Command.KEY_TIMEZONE:
                return String.format("%+03d", TimeZone.getTimeZone((String) value).getRawOffset() / 3600000);
            case Command.KEY_INDEX:
                switch (((Number) value).intValue()) {
                    case 0:
                        return "A";
                    case 1:
                        return "B";
                    case 2:
                        return "C";
                    default:
                        return null;
                }
            default:
                return null;
        }
    }

    @Override
    protected Object encodeCommand(Command command) {

        initDevicePassword(command, "123456");

        switch (command.getType()) {
            case Command.TYPE_SET_TIMEZONE:
                return formatCommand(command, "%sL%s", this, Command.KEY_DEVICE_PASSWORD, Command.KEY_TIMEZONE);
            case Command.TYPE_VOICE_MONITORING:
                return formatCommand(command, "%sP%s", this, Command.KEY_DEVICE_PASSWORD, Command.KEY_ENABLE);
            case Command.TYPE_ALARM_SPEED:
                return formatCommand(command, "%sJ1%s", Command.KEY_DEVICE_PASSWORD, Command.KEY_DATA);
            case Command.TYPE_ALARM_GEOFENCE:
                return formatCommand(command, "%sR1%s", Command.KEY_DEVICE_PASSWORD, Command.KEY_RADIUS);
            case Command.TYPE_ALARM_VIBRATION:
                return formatCommand(command, "%sW1,%s", Command.KEY_DEVICE_PASSWORD, Command.KEY_DATA);
            case Command.TYPE_SET_AGPS:
                return formatCommand(command, "%sAGPS%s", this, Command.KEY_DEVICE_PASSWORD, Command.KEY_ENABLE);
            case Command.TYPE_ALARM_FALL:
                return formatCommand(command, "%sF%s", this, Command.KEY_DEVICE_PASSWORD, Command.KEY_ENABLE);
            case Command.TYPE_MODE_POWER_SAVING:
                return formatCommand(command, "%sSP%s", this, Command.KEY_DEVICE_PASSWORD, Command.KEY_ENABLE);
            case Command.TYPE_MODE_DEEP_SLEEP:
                return formatCommand(command, "%sDS%s", this, Command.KEY_DEVICE_PASSWORD, Command.KEY_ENABLE);
            case Command.TYPE_SOS_NUMBER:
                return formatCommand(command, "%s%s1,%s", this,
                        Command.KEY_DEVICE_PASSWORD, Command.KEY_INDEX, Command.KEY_PHONE);
            case Command.TYPE_SET_INDICATOR:
                return formatCommand(command, "%sLED%s", Command.KEY_DEVICE_PASSWORD, Command.KEY_DATA);
            default:
                return null;
        }
    }

}
