
package org.telescope.app;

import java.security.Principal;

public class UserPrincipal implements Principal {

    private final long userId;

    public UserPrincipal(long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return null;
    }

}
