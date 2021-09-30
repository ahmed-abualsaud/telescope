

package org.traccar.ORM;

import javax.ws.rs.WebApplicationException;
import java.beans.Introspector;
import java.sql.SQLException;
import org.traccar.database.QueryBuilder;
import org.traccar.Context;
import java.util.*;

public class Query {

    private String query;
    private String table;
    private Class<?> clazz;
    private Map<String, Object> queryParams = new LinkedHashMap<>();
    
    public Query(Class<?> clazz) {
        this.clazz = clazz;
        this.table = getObjectsTableName(clazz);
        this.query = "";
    }
    
    private String getObjectsTableName(Class<?> baseModel) {
        String result = "tc_" + Introspector.decapitalize(baseModel.getSimpleName());
        if (!result.endsWith("s")) {result += "s";}
        return result;
    }
    
    public Query select(String... columns) {
        query = "SELECT ";
        for(String column : columns){query += column + ", ";}
        query = query.substring(0, query.length() - 2);
        query += " FROM " + table;
        return this;
    }
    
    public Query where(String column, Object value) {
        if (!query.contains("SELECT")) {
            query = "SELECT * FROM " + table;
        }
        if (query.contains("WHERE")) {
            query += " AND " + column + " = :" + column;
        } else {
            query += " WHERE " + column + " = :" + column;
        }
        queryParams.put(column, value);
        return this;
    }
    
    public Query where(String column, String operator, Object value) {
        if (!query.contains("SELECT")) {
            query = "SELECT * FROM " + table;
        }
        if (query.contains("WHERE")) {
            query += " AND " + column + " " + operator + " :" + column;
        } else {
            query += " WHERE " + column + " " + operator + " :" + column;
        }
        queryParams.put(column, value);
        return this;
    }
    
    public <T> Collection<T> get() {
        try {
            QueryBuilder qb = QueryBuilder.create(Context.getDataManager().getDataSource(), query);
            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                qb = qb.setGeneric(entry.getKey(), entry.getValue());
            }
            return qb.executeQuery((Class<T>) clazz);
        } catch (SQLException e) {throw new WebApplicationException(e);}
    }
    
    public <T> T first() {
        try {
            QueryBuilder qb = QueryBuilder.create(Context.getDataManager().getDataSource(), query);
            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                qb = qb.setGeneric(entry.getKey(), entry.getValue());
            }
            return qb.executeQuerySingle((Class<T>) clazz);
        } catch (SQLException e) {throw new WebApplicationException(e);}
    }
}
