package org.telescope.javel.framework.helper;

public class NumUtil {

    public static Object parse(Object value) {
        if (value != null && value instanceof String) {
            try{return Long.parseLong(value.toString());}
            catch (Exception e) {try{return Double.parseDouble(value.toString());}
            catch (Exception x) {}}
        } return value;
    }
}
