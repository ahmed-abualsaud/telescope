package org.traccar.api.routes;

import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;

public class MainGuard {

     protected Map<String, List<String>> guards = new LinkedHashMap<>();
     
     public boolean isGranted(String guard, String method, String path) {
         boolean matched = false;
         for (Map.Entry<String, List<String>> entry : guards.entrySet()) {
             if (entry.getKey().contains(guard)) {
                 if (matches(entry.getValue(), method, path)) {
                     matched = true;
                     break;
                 }
             }
         }
         return matched;
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
