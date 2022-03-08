package org.telescope.javel.framework.auth;

import java.util.Map;
import java.security.Principal;

public class UserPrincipal implements Principal {

    private final Map<String, Object> user;

    public UserPrincipal(Map<String, Object> user) {
        this.user = user;
    }
    
    public Map<String, Object> getUser() {
        return user;
    }

    public Long getUserId() {
        return Long.parseLong(user.get("id").toString());
    }

    @Override
    public String getName() {
        return null;
    }
}
