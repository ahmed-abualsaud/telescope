
package org.telescope.javel.framework.service.notificator;

import org.telescope.database.StatisticsManager;
import org.telescope.javel.framework.notification.MessageException;
import org.telescope.javel.framework.notification.NotificationFormatter;
import org.telescope.model.Event;
import org.telescope.model.Position;
import org.telescope.model.User;
import org.telescope.server.Context;
import org.telescope.server.Main;

public final class NotificatorSms extends Notificator {

    @Override
    public void sendAsync(long userId, Event event, Position position) {
        final User user = Context.getPermissionsManager().getUser(userId);
        if (user.getPhone() != null) {
            Main.getInjector().getInstance(StatisticsManager.class).registerSms();
            Context.getSmsManager().sendMessageAsync(user.getPhone(),
                    NotificationFormatter.formatShortMessage(userId, event, position), false);
        }
    }

    @Override
    public void sendSync(long userId, Event event, Position position) throws MessageException, InterruptedException {
        final User user = Context.getPermissionsManager().getUser(userId);
        if (user.getPhone() != null) {
            Main.getInjector().getInstance(StatisticsManager.class).registerSms();
            Context.getSmsManager().sendMessageSync(user.getPhone(),
                    NotificationFormatter.formatShortMessage(userId, event, position), false);
        }
    }

}
