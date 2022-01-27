package org.traccar.qruzcab;

import org.traccar.qruzcab.database.QueryBuilder;
import org.traccar.qruzcab.database.DataManager;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.helper.Log;

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

