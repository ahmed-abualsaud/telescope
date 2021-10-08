
package org.traccar.validator;

import java.util.*;

public final class Validator {

    private boolean status;
    private List<String> errors = new LinkedList<String>();
    
    private static Map<String, String> errorMessages = new HashMap<String, String>() {{
        put("exists", "Entry does not exists");
        put("unique", "Entry must be unique");
        put("required", "Entry is required");
    }};
    
    private Validator(boolean status, List<String> errors) {
        this.status = status;
        this.errors = errors;
    }

    public static Validator validate(Map<String, Object> data, Map<String, String> validationArray) {
        String key;
        Object val;
        List<String> validationQueries;
        List<String> messages = new LinkedList<String>();
        boolean result = true, ret;
        for (Map.Entry<String, String> entry : validationArray.entrySet()) {
            key = entry.getKey();
            val = data.get(key);
            validationQueries = new LinkedList<String>(Arrays.asList(entry.getValue().split("\\|")));
            if (validationQueries.contains("multiple")) {
                validationQueries.removeIf("multiple"::equals);
                for (Object value : (List<Object>) val ) {
                    for (String query : validationQueries) {
                        if (query.contains(":")) {
                            String[] params = query.split(":");
                            if (params[1].contains(".")) {
                                String[] params2 = params[1].split("\\.");
                                ret = Command.getCommand(params[0], params2[1], value, params2[0]);
                            } else {ret = Command.getCommand(params[0], key, value, params[1]);}
                            query = params[0];
                        } else {ret = Command.getCommand(query, value);}
                        if (!ret) {
                            result = false;
                            messages.add( key + " = " + String.valueOf(value) + ": " + errorMessages.get(query));
                        }
                    }
                }
            } else {
                for (String query : validationQueries) {
                    if (query.contains(":")) {
                        String[] params = query.split(":");
                        if (params[1].contains(".")) {
                            String[] params2 = params[1].split("\\.");
                            ret = Command.getCommand(params[0], params2[1], val, params2[0]);
                        } else {ret = Command.getCommand(params[0], key, val, params[1]);}
                        query = params[0];
                    } else {ret = Command.getCommand(query, val);}
                    if (!ret) {
                        result = false;
                        messages.add( key + " = " + String.valueOf(val) + ": " + errorMessages.get(query));
                    }
                }
            }
        }
        return new Validator(result, messages);
    }
 
    public boolean validated() {
        return status;
    }
    
    public List<String> getErrors() {
    	return errors;
    }
}
