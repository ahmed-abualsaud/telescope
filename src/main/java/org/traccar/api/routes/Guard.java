package org.traccar.api.routes;

import static java.util.Arrays.asList;

public class Guard extends MainGuard {
     
     public Guard() {
     
         guards.put("all", asList(
             // Routes accessable by all users (admins, drivers, users)
             
             "POST:/api/admin/register",
             "POST:/api/admin/login",
             
             "POST:/api/user/register",
             "POST:/api/user/login",
             
             "POST:/api/driver/register",
             "POST:/api/driver/login"
         ));
         
         guards.put("admin", asList(
             // Routes accessable by admins
             
             "GET:/api/admin",
             "PUT:/api/admin",
             "DELETE:/api/admin",
             
             "PUT:/api/user/{id}"
         ));
         
         guards.put("user", asList(
             // Routes accessable by users
             
             "GET:/api/user",
             "PUT:/api/user",
             "DELETE:/api/user"
         ));
         
         guards.put("driver", asList(
             // Routes accessable by drivers
             
             "GET:/api/driver",
             "PUT:/api/driver",
             "DELETE:/api/driver"
         ));
         
         guards.put("admin, user", asList(
             // Routes accessable by admins and drivers
             
             
         ));
         
         guards.put("admin, driver", asList(
             // Routes accessable by admins and users
             
             
         ));
         
         guards.put("user, driver", asList(
             // Routes accessable by users and drivers
             
             
         ));
         
     }
}
