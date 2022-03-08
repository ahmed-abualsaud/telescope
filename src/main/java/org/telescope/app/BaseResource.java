
package org.telescope.app;

import javax.ws.rs.core.SecurityContext;

import org.telescope.javel.framework.auth.UserPrincipal;

public class BaseResource {

    @javax.ws.rs.core.Context
    private SecurityContext securityContext;

    protected long getUserId() {
        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();
        if (principal != null) {
            return principal.getUserId();
        }
        return 0;
    }
}
