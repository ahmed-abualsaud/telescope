
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.Protocol;
import org.telescope.server.StringProtocolEncoder;

public class ItsProtocolEncoder extends StringProtocolEncoder {

    public ItsProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_ENGINE_STOP:
                return "@SET#RLP,OP1,";
            case Command.TYPE_ENGINE_RESUME:
                return "@CLR#RLP,OP1,";
            default:
                return null;
        }
    }

}
