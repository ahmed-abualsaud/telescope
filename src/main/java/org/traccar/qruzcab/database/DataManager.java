package org.traccar.qruzcab.database;

import org.traccar.config.Config;
import org.traccar.config.Keys; 
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class DataManager {

    private final Config config;
    
    private DataSource dataSource;

     public DataSource getDataSource() {
        return dataSource;
    }
    
    public DataManager(Config config) throws Exception {
        this.config = config;
        initDatabase();
    }
    
    private void initDatabase() throws Exception {
        String driver = config.getString(Keys.QRUZCAB_DATABASE_DRIVER);
        if (driver != null) {
            Class.forName(driver);
        }
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driver);
        hikariConfig.setJdbcUrl(config.getString(Keys.QRUZCAB_DATABASE_URL));
        hikariConfig.setUsername(config.getString(Keys.QRUZCAB_DATABASE_USER));
        hikariConfig.setPassword(config.getString(Keys.QRUZCAB_DATABASE_PASSWORD));
        hikariConfig.setConnectionInitSql(config.getString(Keys.DATABASE_CHECK_CONNECTION));
        hikariConfig.setIdleTimeout(600000);

        dataSource = new HikariDataSource(hikariConfig);
    }

}
