

package org.traccar.ORM;

import javax.ws.rs.WebApplicationException;
import org.traccar.validator.Command;
import java.util.List;
import java.util.stream.Collectors;

public class RelationalModel {

    
    protected static Class<?> clazz;
    
    public static Query select(String... columns) {
        Query query = new Query(clazz);
        return query.select(columns);
    }
    
    public static Query where(String column, Object value) {
        Query query = new Query(clazz);
        return query.where(column, value);
    }
    
    public static Query where(String column, String operator, Object value) {
        Query query = new Query(clazz);
        return query.where(column, operator, value);
    }
}
