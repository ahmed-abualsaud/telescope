package org.traccar.validator;

import javax.ws.rs.WebApplicationException;
import java.sql.SQLException;

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
    
    private static Class<?> getClassByName(String className) throws ClassNotFoundException {
        switch (className) {
            case "device":
                return Device.class;
            case "user":
                return User.class;
            case "position":
                return Position.class;
            default:
                throw new ClassNotFoundException();
        }
    }
    
    public static boolean required(Object value) {
        if (value == null) {
            return false;
        }
        return true;
    }

    public static boolean exists(String columnName, Object value, String className) {
        try {
            Class<BaseModel> baseCalss = (Class<BaseModel>) getClassByName(className);
            try {
                return Context.getManager(baseCalss).exists(columnName, value);
            } catch (SQLException e) {
                throw new WebApplicationException(e);
            }
        } catch (ClassNotFoundException e) {
            throw new WebApplicationException(e);
        }
    }
    
    public static boolean unique(String columnName, Object value, String className) {
        try {
            Class<BaseModel> baseCalss = (Class<BaseModel>) getClassByName(className);
            try {
                if (value == null) {return false;}
                return !Context.getManager(baseCalss).exists(columnName, value);
            } catch (SQLException e) {
                throw new WebApplicationException(e);
            }
        } catch (ClassNotFoundException e) {
            throw new WebApplicationException(e);
        }
    }
}
