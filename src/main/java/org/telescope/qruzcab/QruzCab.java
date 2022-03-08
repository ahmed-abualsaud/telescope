package org.telescope.qruzcab;

import org.telescope.qruzcab.database.QueryBuilder;
import org.telescope.qruzcab.database.DataManager;
import org.telescope.config.Config;
import org.telescope.config.Keys;
import org.telescope.javel.framework.helper.Log;

public final class QruzCab {

    private static Config config;
    
    private static DataManager dataManager;
    
    public static void init(String configFile) throws Exception {
        try {
            config = new Config(configFile);
            Log.setupLogger(config);
        } catch (Exception e) {
            config = new Config();
            Log.setupDefaultLogger();
            throw e;
        }
        if (config.hasKey(Keys.QRUZCAB_DATABASE_URL)) {
            dataManager = new DataManager(config);
        }
    }
    
    public static QueryBuilder table(String tableName) {
        return new QueryBuilder(tableName, dataManager.getDataSource());
    }

}

