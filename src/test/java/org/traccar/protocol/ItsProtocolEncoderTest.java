package org.telescope.protocol;

import org.junit.Test;
import org.telescope.ProtocolTest;
import org.telescope.model.Command;

import static org.junit.Assert.assertEquals;

public class ItsProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncode() throws Exception {

        var encoder = new ItsProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_ENGINE_STOP);

        assertEquals("@SET#RLP,OP1,", encoder.encodeCommand(command));

    }

}
