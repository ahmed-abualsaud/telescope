
package org.telescope.handler;

import io.netty.channel.ChannelHandler;

import org.telescope.database.IdentityManager;
import org.telescope.model.Position;
import org.telescope.server.BaseDataHandler;

@ChannelHandler.Sharable
public class CopyAttributesHandler extends BaseDataHandler {

    private IdentityManager identityManager;

    public CopyAttributesHandler(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    @Override
    protected Position handlePosition(Position position) {
        String attributesString = identityManager.lookupAttributeString(
                position.getDeviceId(), "processing.copyAttributes", "", false, true);
        Position last = identityManager.getLastPosition(position.getDeviceId());
        if (last != null) {
            for (String attribute : attributesString.split("[ ,]")) {
                if (last.getAttributes().containsKey(attribute) && !position.getAttributes().containsKey(attribute)) {
                    position.getAttributes().put(attribute, last.getAttributes().get(attribute));
                }
            }
        }
        return position;
    }

}
