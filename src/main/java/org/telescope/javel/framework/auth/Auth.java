package org.telescope.javel.framework.auth;

import org.telescope.javel.framework.helper.Hashing;
import org.telescope.javel.framework.storage.database.DB;

import java.util.Map;
import java.security.Principal;
import javax.ws.rs.core.SecurityContext;

public class Auth implements SecurityContext {

    private UserPrincipal principal;

    public Auth(UserPrincipal principal) {
        this.principal = principal;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return true;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
    
    public static Map<String, Object> attempt(Map<String, Object> credentials, String table) {
        Map<String, Object> user = null;
        if (credentials.containsKey("email") && credentials.get("email") != null) {
            user = DB.table(table).where("email", credentials.get("email")).first(true);
        }
        if (credentials.containsKey("phone") && credentials.get("phone") != null) {
            user = DB.table(table).where("phone", credentials.get("phone")).first(true);
        }
        if (credentials.containsKey("password") && credentials.get("password") != null && user != null) {
            boolean validPassword = Hashing.validatePassword(credentials.get("password").toString(), 
                    user.get("password").toString(), user.get("salt").toString());
            if (!validPassword) {user = null;}
            if (user.containsKey("disabled") && user.get("disabled").toString().equals("true")) {return null;}
            user.remove("password");
            user.remove("salt");
        }
        return user;
    }
}
