package org.telescope.javel.framework.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.server.Context;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JSON {

    private static Logger LOGGER = LoggerFactory.getLogger(JSON.class);
    
    public static Map<String, Object> decode(Object message) {
        return Context.getObjectMapper().convertValue(message, Map.class);
    }
    
    public static boolean valid(String message) {
        try {
            Context.getObjectMapper().readValue(message, Map.class);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    public static Map<String, Object> decode(String message) {
        Map<String, Object> data = null;
        try {
            data = Context.getObjectMapper().readValue(message, Map.class);
        } catch (JsonProcessingException e) {
            LOGGER.warn("JSON Decoding Error: ", e);
        }
        return data;
    }
    
    public static String encode(Map<String, Object> data) {
        String json = null;
        try {
            json = Context.getObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            LOGGER.warn("JSON Encoding Error: ", e);
        }
        return json;
    }
}
