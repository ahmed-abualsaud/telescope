

package org.traccar.ORM;

import javax.ws.rs.WebApplicationException;
import org.traccar.validator.Command;
import java.util.List;
import java.lang.StackWalker.StackFrame;
import java.util.Optional;
import java.security.CodeSource;
import java.net.URISyntaxException;
import java.io.IOException;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Paths;
import java.nio.file.Files;

public class RelationalModel {

    private static Class<?> clazz;
    private static Logger LOGGER = LoggerFactory.getLogger(RelationalModel.class);
    
    public static Query select(String... columns) {
        StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        Class<?> callerClass = stackWalker.getCallerClass();
        Optional<StackFrame> stackFrame = stackWalker.walk(s -> s.filter(f -> f.getDeclaringClass().equals(callerClass)).findFirst());
        //String className = getCallerModelName(stackFrame.get().getLineNumber());
        //throw new WebApplicationException(stackFrame.get().getFileName​());
        throw new WebApplicationException(getCallerModelName(callerClass, stackFrame.get().getLineNumber()));
        //Query query = new Query(clazz);
        //return query.select(columns);
    }
    
    public static Query where(String column, Object value) {
        Query query = new Query(clazz);
        return query.where(column, value);
    }
    
    public static Query where(String column, String operator, Object value) {
        Query query = new Query(clazz);
        return query.where(column, operator, value);
    }
    
    private static String getCallerModelName(Class<?> clazz, int lineNumber) {
        try {
            String path = getClassFullPath(clazz);
            String line = Files.readAllLines(Paths.get(path)).get(lineNumber - 1);
            return line;
        } catch (IOException ex) {
            LOGGER.error("Can not get caller model name", ex);
            return null;
        }
    }
    
    private static String getClassFullPath(Class<?> clazz) {
        try{
            CodeSource codeSource = RelationalModel.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            String jarDir = jarFile.getParentFile().getParentFile().getPath();
            return jarDir + "/src/main/java/" + clazz.getCanonicalName().replace('.','/') + ".java";
        } catch (URISyntaxException ex) {
            LOGGER.error("URI Syntax Exception", ex);
            return null;
        }
    }
}
