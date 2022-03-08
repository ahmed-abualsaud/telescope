package org.telescope.protocol;

import org.junit.Test;
import org.telescope.ProtocolTest;
import org.telescope.model.Command;

import static org.junit.Assert.assertEquals;

public class EasyTrackProtocolEncoderTest extends ProtocolTest {

    @Test
    public void testEncodeEngineStop() {

        var encoder = new EasyTrackProtocolEncoder(null);

        Command command = new Command();
        command.setDeviceId(1);
        command.setType(Command.TYPE_ENGINE_STOP);

        assertEquals("*ET,123456789012345,FD,Y1#", encoder.encodeCommand(command));

    }

}
