package org.traccar.validator;

import javax.ws.rs.WebApplicationException;
import java.sql.SQLException;
import org.traccar.ORM.RelationalModel;
import org.traccar.Context;
import org.traccar.model.Device;
import org.traccar.model.User;
import org.traccar.model.Position;
import org.traccar.model.BaseModel;

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
    
    public static RelationalModel getOrm(String className) throws ClassNotFoundException {
        
        switch (className) {
            case "device":
                return new RelationalModel(Device.class);
            case "user":
                return new RelationalModel(User.class);
            case "position":
                return new RelationalModel(Position.class);
            default:
                throw new ClassNotFoundException();
        }
    }
    
    public static boolean required(Object value) {
        if (value == null) {return false;}
        return true;
    }

    public static <T extends BaseModel> boolean exists(String columnName, Object value, String className) {
        try {
            Object result = getOrm(className).select("id").where(columnName, value).first();
            if (result == null) {return false;}
            else {return true;}
        } catch (ClassNotFoundException e) {
            throw new WebApplicationException(e);
        } 
    }
    
    public static boolean unique(String columnName, Object value, String className) {
        if (value == null) {return false;}
        return !exists(columnName, value, className);
    }
}
