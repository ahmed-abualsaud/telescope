package org.telescope.javel.framework.helper;

public class BoolUtil {

    public static Object parse(Object value) {
        if (value != null && value instanceof String) {
            if (value.toString().equalsIgnoreCase("true")) {return true;}
            if (value.toString().equalsIgnoreCase("false")) {return false;}
        } return value;
    }
}
