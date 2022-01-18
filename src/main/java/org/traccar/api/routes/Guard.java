package org.traccar.api.routes;

import java.util.List;
import static java.util.Arrays.asList;

public class Guard {

     private List<String> all;
     private List<String> admin;
     private List<String> user;
     private List<String> driver;
     private List<String> admin_user;
     private List<String> admin_driver;
     private List<String> user_driver;
     
     public Guard() {
     
         all = asList(
             // Routes accessable by all users (admins, drivers, users)
             "POST:/api/user/register",
             "POST:/api/user/login"
         );
         
         admin = asList(
             // Routes accessable by admins
             "PUT:/api/user/{id}"
         );
         
         user = asList(
             // Routes accessable by users
             "GET:/api/user",
             "PUT:/api/user",
             "DELETE:/api/user"
         );
         
         driver = asList(
             // Routes accessable by drivers
         
         );
         
         admin_user = asList(
             // Routes accessable by admins and drivers
             
         );
         
         admin_driver = asList(
             // Routes accessable by admins and users
             
         );
         
         user_driver = asList(
             // Routes accessable by users and drivers
         
         );
     }
     
     //============================================================================================================

     public boolean isGranted(String guard, String method, String path) {
     
        if (guard.equals("user"))  {return matches(user, method, path);}
        if (guard.equals("driver")){return matches(driver, method, path);}
        if (guard.equals("admin")) {return matches(admin, method, path);}
        if (guard.equals("all"))   {return matches(all, method, path);}
        return false;
     }
     
     private boolean matches(List<String> routes, String method, String path) {
         String[] route;
         boolean matched = false;
         for (int i = 0; i < routes.size(); i++) {
             route = routes.get(i).split(":");
             if (route[0].equals(method)) {
                 if(matchDirs(route[1].split("/"), path.split("/"))) {
                     matched = true;
                     break;
                 }
             }
         }
         return matched;
     }
     
     private boolean matchDirs(String[] routeDirs, String[] pathDirs) {
         if (routeDirs.length != pathDirs.length) {return false;}
         boolean matched = true;
         for (int j = 1; j < routeDirs.length; j++) {
             if (!routeDirs[j].startsWith("{") && !routeDirs[j].endsWith("}")) {
                 matched = (matched && routeDirs[j].equals(pathDirs[j]));
             }
         }
         return matched;
     }
}
