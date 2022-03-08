package org.telescope.javel.framework.storage.database;

import org.telescope.server.Context;

public class DB {
    
    public static Eloquent table(String tableName) {
        return new Eloquent(tableName, Context.getDataManager().getDataSource());
    }
}
