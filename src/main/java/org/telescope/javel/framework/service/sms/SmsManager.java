
package org.telescope.javel.framework.service.sms;

import org.telescope.javel.framework.notification.MessageException;

public interface SmsManager {

    void sendMessageSync(
            String destAddress, String message, boolean command) throws InterruptedException, MessageException;

    void sendMessageAsync(
            String destAddress, String message, boolean command);

}
