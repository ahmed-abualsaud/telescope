package org.telescope.app.model;

import org.telescope.javel.framework.model.Model;

public class Maintenance extends Model {

    private static String[] types = {
        "ALARM",
        "FUEL_LEVEL", 
        "FUEL_CONSUMPTION", 
        "FUEL_USED_LITERS", 
        "SPEED_METERS",
        "MOTION_STATUS", 
        "BATTERY_LEVEL", 
        "BATTERY_CHARGING", 
        "DISTANCE_METERS", 
        "DURATION_SECONDS",
        "IGNITION_STATUS", 
        "ACCELERATION_METERS"
    };

    public Maintenance() {}
    
    public static String[] getTypes() {
        return types;
    }

    public static boolean checkMaintenance(String type) {
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(type)) {return true;}
        }
        return false;
    }
}
