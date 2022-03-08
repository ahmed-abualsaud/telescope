package org.telescope.app.model;

import java.util.Map;

import org.telescope.javel.framework.model.Model;

import java.util.LinkedHashMap;

public class Position extends Model {
    
    public Position() {
        json = "motion,distance,duration,speed,acceleration,battery,engine,attributes,network";
    }
    
    public static Map<String, Object> getBatteryValue(Map<String, Object> battery) {
        double value;
        Map<String, Object> newBattery = new LinkedHashMap<>();
        value = ((Number) battery.get("power")).doubleValue();
        if (value != -1) {newBattery.put("power", value);}
        value = ((Number) battery.get("battery")).doubleValue();
        if (value != -1) {newBattery.put("battery", value);}
        value = ((Number) battery.get("battery_level")).doubleValue();
        if (value != -1) {newBattery.put("battery_level", value);}
        return newBattery;
    }
}
