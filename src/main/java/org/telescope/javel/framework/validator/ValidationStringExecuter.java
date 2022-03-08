
package org.telescope.javel.framework.validator;

import java.util.*;

public final class ValidationStringExecuter {

    private String key;
    private Object val;
    private List<String> validationQueries = new ArrayList<String>();
    
    private Map<String, String> errorMessages = new HashMap<String, String>() {{
        put("exists", "Entry does not exists");
        put("unique", "Entry must be unique and can not be dublicated or null");
        put("required", "Entry is required and can not be null");
    }};
    
    private ValidationStringExecuter(String key, Object val, List<String> validationQueries) {
        this.key = key;
        this.val = val;
        this.validationQueries = validationQueries;
    }


    public static ValidationStringExecuter extractQueries(String key, Object val, String validationString) {
        return new ValidationStringExecuter(key, val, Arrays.asList(validationString.split("\\|")));
    }
    
    public Map<String, Object> execute() {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> temp1 = new LinkedHashMap<>();
        Map<String, Object> temp2 = new LinkedHashMap<>();
        if (validationQueries.contains("multiple")) {
            validationQueries.removeIf("multiple"::equals);
            for (Object value : (List<Object>) val ) {
                temp1 = parseQueries(value);
            }
        } else {temp2 = parseQueries(val);}
        response.put("status", (boolean) temp1.get("status") && (boolean) temp2.get("status"));
        response.put("messages", ((List<String>) temp1.get("messages")).addAll((List<String>) temp2.get("messages")));
        return response;
    }
    
    private Map<String, Object> parseQueries(Object val) {
        Map<String, Object> response = new LinkedHashMap<>();
        List<String> messages = new LinkedList<String>();
        boolean result = true, ret;
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
        response.put("status", result);
        response.put("messages", messages);
        return response;
    }
}
