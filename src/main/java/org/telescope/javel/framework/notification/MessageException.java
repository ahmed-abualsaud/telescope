
package org.telescope.javel.framework.notification;

public class MessageException extends Exception {

    public MessageException(Throwable cause) {
        super(cause);
    }

    public MessageException(String message) {
        super(message);
    }

}
