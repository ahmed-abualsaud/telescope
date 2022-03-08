package org.telescope.protocol;

import org.junit.Test;
import org.telescope.ProtocolTest;
import org.telescope.model.Command;

import static org.junit.Assert.assertEquals;

public class MiniFinderProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncode() throws Exception {

        var encoder = new MiniFinderProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_SET_TIMEZONE);
        command.set(Command.KEY_TIMEZONE, "GMT+1");

        assertEquals("123456L+01", encoder.encodeCommand(command));

        command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_SOS_NUMBER);
        command.set(Command.KEY_INDEX, 2);
        command.set(Command.KEY_PHONE, "1111111111");

        assertEquals("123456C1,1111111111", encoder.encodeCommand(command));

    }

}
