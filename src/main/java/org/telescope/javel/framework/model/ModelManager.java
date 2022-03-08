package org.telescope.javel.framework.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.config.Keys;
import org.telescope.server.Context;

import java.io.File;
import java.util.Map;
import java.util.LinkedHashMap;

public class ModelManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelManager.class);
    private final Map<String, Map<String, Object>> allProperties = new LinkedHashMap<>();

    public ModelManager() {
        try {
            String path = Context.getConfig().getString(Keys.APP_DIRECTORY);
            path += "/telescope/src/main/java/org/telescope/app/model";
            String pack = "org.telescope.app.model";
            File[] files = new File(path).listFiles();
            String fileName;
            Model model;
        
            for (int i = 0; i < files.length; i++) {
                fileName = files[i].getName();
                if (files[i].isFile() && fileName.contains(".java") && 
                    !fileName.equals("ModelManager.java") && !fileName.equals("Model.java")) {
                    fileName = fileName.substring(0, fileName.length() - 5);
                    model = (Model) Class.forName(pack + "." + fileName).getConstructor().newInstance();
                    allProperties.put(fileName.toLowerCase(), model.getProperties());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Model Manager Error: ", e);
        }
    }
    
    public Map<String, Map<String, Object>> getAllProperties() {
        return allProperties;
    }
}
