package org.traccar.api.modelconf;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;

import org.traccar.Context;
import org.traccar.config.Keys;

import java.util.Map;
import java.util.Properties;
import java.util.LinkedHashMap;


public class ModelConf {

    private final Map<String, Properties> modelConf = new LinkedHashMap<>();
    
    public ModelConf() {
        String path = Context.getConfig().getString(Keys.APP_DIRECTORY);
        path += "/telescope/src/main/java/org/traccar/api/modelconf";
        File[] files = new File(path).listFiles();
        String fileName;
        for (int i = 0; i < files.length; i++){
            fileName = files[i].getName();
            if (files[i].isFile() && fileName.contains(".xml") && !fileName.equals("ModelConf.java")) {
                modelConf.put(fileName.substring(0, fileName.length() - 4).toLowerCase(), 
                    getProperties(files[i].getAbsolutePath()));
            }
        }
    }
    
    public Map<String, Properties> getModelConf() {
        return modelConf;
    }
    
    private Properties getProperties(String file) {
        try {
            Properties properties = new Properties();
            properties.loadFromXML(new FileInputStream(file));
            return properties;
        } catch (Exception e) {
            throw new RuntimeException("Load ModelConf Error: ", e);
        }
    }
}
