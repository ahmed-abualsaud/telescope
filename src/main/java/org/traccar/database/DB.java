package org.traccar.database;

import org.traccar.Context;

public class DB {
    
    public static Eloquent table(String tableName) {
        return new Eloquent(tableName, Context.getDataManager().getDataSource());
    }
}
