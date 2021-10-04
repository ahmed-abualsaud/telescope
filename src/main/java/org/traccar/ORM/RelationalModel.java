

package org.traccar.ORM;

import javax.ws.rs.WebApplicationException;
import org.traccar.validator.Command;
import java.util.List;
import java.lang.StackWalker.StackFrame;
import java.util.Optional;

public class RelationalModel {

    
    protected static Class<?> clazz;
    
    public static Query select(String... columns) {
        //StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        //Class<?> callerClass = stackWalker.getCallerClass();
        //Optional<StackFrame> stackFrame = stackWalker.walk(s -> s.filter(f -> f.getDeclaringClass().equals(callerClass)).findFirst());
        //throw new WebApplicationException(stackFrame.get().getLineNumber());
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
