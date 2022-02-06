package org.traccar.api.route;

import static java.util.Arrays.asList;

public class Guard extends MainGuard {
     
     public Guard() {
     
         guards.put("public", asList(
             // Public routes that can be accessed by unregistered users.

             "GET:/api/test",

             "POST:/api/admin/register",
             "POST:/api/admin/login",

             "POST:/api/user/register",
             "POST:/api/user/login",

             "POST:/api/driver/register",
             "POST:/api/driver/login"
         ));
         
         guards.put("common", asList(
             // Common routes that can be accessed by any of the registered users.
             
             "POST:/api/broadcasting/auth"
         ));
         
         guards.put("admin", asList(
             // Routes accessable by admins.
             
             "GET:/api/admin",
             "PUT:/api/admin",
             "DELETE:/api/admin",

             "GET:/api/admin/devices",
             "GET:/api/admin/user/{user_id}/devices",

             "GET:/api/admin/device/id/{device_id}",
             "DELETE:/api/admin/device/id/{device_id}",

             "GET:/api/admin/device/unique/id/{unique_id}",
             "DELETE:/api/admin/device/unique/id/{device_id}",

             "POST:/api/admin/add/device/to/user/{user_id}",
             "PUT:/api/admin/update/device/{device_id}",

             "GET:/api/admin/drivers",
             "GET:/api/admin/user/{user_id}/drivers",
             
             "GET:/api/admin/driver/id/{driver_id}",
             "DELETE:/api/admin/driver/id/{driver_id}",
             "POST:/api/admin/add/driver/to/user/{user_id}",
             "PUT:/api/admin/update/driver/{driver_id}",

             "GET:/api/admin/get/last/position/{unique_id}"
         ));
         
         guards.put("user", asList(
             // Routes accessable by users.
             
             "GET:/api/user",
             "PUT:/api/user",
             "DELETE:/api/user",

             "GET:/api/user/devices",
             "POST:/api/user/device",

             "GET:/api/user/device/id/{device_id}",
             "PUT:/api/user/device/id/{device_id}",
             "DELETE:/api/user/device/id/{device_id}",

             "GET:/api/user/device/unique/id/{unique_id}",
             "DELETE:/api/user/device/unique/id/{device_id}",

             "GET:/api/user/drivers",
             "POST:/api/user/driver",
             
             "GET:/api/user/driver/id/{driver_id}",
             "PUT:/api/user/driver/id/{driver_id}",
             "DELETE:/api/user/driver/id/{driver_id}",
             
             "GET:/api/user/get/last/position/{unique_id}"
         ));
         
         guards.put("driver", asList(
             // Routes accessable by drivers.

             "GET:/api/driver",
             "PUT:/api/driver",
             "DELETE:/api/driver"
         ));
         
         guards.put("admin, user", asList(
             // Routes accessable by admins and drivers.

             
         ));
         
         guards.put("admin, driver", asList(
             // Routes accessable by admins and users.

             
         ));
         
         guards.put("user, driver", asList(
             // Routes accessable by users and drivers.

             
         ));
         
     }
}
