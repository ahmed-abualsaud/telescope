package org.telescope.javel.framework.model;

import java.util.Map;
import java.util.LinkedHashMap;

public class Model {

    protected String json = null;
    protected String table = null;
    protected String ignored = null;
    protected boolean timestamps = false;
    protected Map<String, Object> properties = new LinkedHashMap<>();
    
    protected Map<String, Object> getProperties() {
        properties.put("json", json);
        properties.put("table", table);
        properties.put("ignored", ignored);
        properties.put("timestamps", timestamps);
        return properties;
    }
}
