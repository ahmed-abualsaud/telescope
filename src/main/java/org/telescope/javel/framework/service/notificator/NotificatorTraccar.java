
package org.telescope.javel.framework.service.notificator;

import org.telescope.config.Keys;
import org.telescope.server.Context;

public class NotificatorTraccar extends NotificatorFirebase {

    public NotificatorTraccar() {
        super(
                "https://www.traccar.org/push/",
                Context.getConfig().getString(Keys.NOTIFICATOR_telescope_KEY));
    }

}
