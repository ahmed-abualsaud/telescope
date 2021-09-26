

package org.traccar.ORM;

import java.lang.reflect.ParameterizedType;
import java.beans.Introspector;
import java.sql.SQLException;
import org.traccar.model.BaseModel;
import org.traccar.database.QueryBuilder;
import org.traccar.Context;
import java.util.*;

public final class RelationalModel<T extends BaseModel> {
    protected String query;
    public Class<T> clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    protected String table = getObjectsTableName(clazz);
    protected Map<String, Object> queryParams = new LinkedHashMap<>();
    
    public RelationalModel() {}
    
    private static String getObjectsTableName(Class<?> baseModel) {
        String result = "tc_" + Introspector.decapitalize(baseModel.getSimpleName());
        if (!result.endsWith("s")) {result += "s";}
        return result;
    }
    
    public static RelationalModel select(String... columns) {
        RelationalModel rm = new RelationalModel();
        rm.query = "SELECT ";
        for(String column : columns){rm.query += column + " ";}
       rm.query += "FROM " + rm.table;
       return rm;
    }
    
    public static RelationalModel allWhere(String column, Object value) {
        RelationalModel rm = new RelationalModel();
        rm.query = "SELECT * FROM " + rm.table + " WHERE " + column + " = :" + column;
        rm.queryParams.put(column, value);
        return rm;
    }
    
    public static RelationalModel allWhere(String column, String operator, Object value) {
        RelationalModel rm = new RelationalModel();
        rm.query = "SELECT * FROM " + rm.table + " WHERE " + column + " " + operator + " :" + column;
        rm.queryParams.put(column, value);
        return rm;
    }
    
    public RelationalModel where(String column, Object value) {
        if (query.contains("WHERE")) {
            query += " AND " + column + " = :" + column;
        } else {
            query += " WHERE " + column + " = :" + column;
        }
        queryParams.put(column, value);
        return this;
    }
    
    public RelationalModel where(String column, String operator, Object value) {
        if (query.contains("WHERE")) {
            query += " AND " + column + " " + operator + " :" + column;
        } else {
            query += " WHERE " + column + " " + operator + " :" + column;
        }
        queryParams.put(column, value);
        return this;
    }
    
    public Collection<T> get() throws SQLException {
        QueryBuilder qb = QueryBuilder.create(Context.getDataManager().getDataSource(), query);
        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            qb = qb.setGeneric(entry.getKey(), entry.getValue());
        }
        return qb.executeQuery(clazz);
    }
}
