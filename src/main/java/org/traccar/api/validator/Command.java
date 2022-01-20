package org.traccar.api.validator;

import org.traccar.database.DB;
import java.util.*;

public final class Command {

    public static boolean getCommand(String command, String columnName, Object value, String className) {
        switch (command) {
            case "exists":
                return exists(columnName, value, className);
            case "unique":
                return unique(columnName, value, className);
            default:
                throw new IllegalArgumentException("Unknown action");
        }
    }
    
    public static boolean getCommand(String command, Object value) {
       switch (command) {
            case "required":
                return required(value);
            default:
                throw new IllegalArgumentException("Unknown action");
        }
    }
    
    private static boolean required(Object value) {
        if (value == null) {return false;}
        return true;
    }

    public static boolean exists(String columnName, Object value, String table) {
        if (value == null) {return true;}
        List<Map<String, Object>> result = DB.table(table).select("id").where(columnName, value).get();
        if (result == null) {return false;}
        return true;
    }
    
    private static boolean unique(String columnName, Object value, String table) {
        if (value == null) {return true;}
        List<Map<String, Object>> result = DB.table(table).select("id").where(columnName, value).get();
        if (result == null) {return true;}
        /*if(result.size() == 1 &&
           (table == "user") && 
           Long.parseLong(result.get(0).get("id").toString()) == userId) {
            return true;
        }*/
        return false;
    }
}
