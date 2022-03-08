
package org.telescope.javel.framework.service.notificator;

import org.telescope.javel.framework.notification.FullMessage;
import org.telescope.javel.framework.notification.MessageException;
import org.telescope.javel.framework.notification.NotificationFormatter;
import org.telescope.model.Event;
import org.telescope.model.Position;
import org.telescope.server.Context;

import javax.mail.MessagingException;

public final class NotificatorMail extends Notificator {

    @Override
    public void sendSync(long userId, Event event, Position position) throws MessageException {
        try {
            FullMessage message = NotificationFormatter.formatFullMessage(userId, event, position);
            Context.getMailManager().sendMessage(userId, message.getSubject(), message.getBody());
        } catch (MessagingException e) {
            throw new MessageException(e);
        }
    }

}
