package org.telescope.javel.framework.validator;

import java.util.*;

import org.telescope.javel.framework.storage.database.DB;

public final class Command {

    public static boolean getCommand(String command, String nullcol, String column, Object value, String table) {
       switch (command) {
            case "isset":
                return isset(nullcol, column, value, table);
            default:
                throw new IllegalArgumentException("Unknown action");
        }
    }

    public static boolean getCommand(String command, String column, Object value, String table) {
        switch (command) {
            case "exists":
                return exists(column, value, table);
            case "unique":
                return unique(column, value, table);
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

    public static boolean exists(String column, Object value, String table) {
        if (value == null) {return true;}
        List<Map<String, Object>> result = DB.table(table).select("id").where(column, value).get();
        if (result == null) {return false;}
        return true;
    }
    
    private static boolean unique(String column, Object value, String table) {
        if (value == null) {return true;}
        List<Map<String, Object>> result = DB.table(table).select("id").where(column, value).get();
        if (result == null) {return true;}
        /*if(result.size() == 1 &&
           (table == "user") && 
           Long.parseLong(result.get(0).get("id").toString()) == userId) {
            return true;
        }*/
        return false;
    }

    public static boolean isset(String nullcol, String column, Object value, String table) {
        if (value == null) {return true;}
        column = (column.equals(table.substring(0, table.length() - 1) + "_id") ? "id" : column);
        Map<String, Object> result = DB.table(table).select(nullcol).where(column, value).first();
        if (result == null) {return true;}
        if (result.get(nullcol) == null) {return false;}
        return true;
    }
}
