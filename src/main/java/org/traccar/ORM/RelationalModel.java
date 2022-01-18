

package org.traccar.ORM;

import javax.ws.rs.WebApplicationException;
import org.traccar.api.validator.Command;
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
        Optional<StackFrame> stackFrame = stackWalker.walk(s -> s.filter(
            f -> f.getDeclaringClass().getSuperclass().equals(callerClass)
        ).findFirst());
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
/*  
    
    
    
    
    public static Map<String, Object> test() {
        StackWalker sw = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        Optional<StackFrame> stackFrame = stackWalker.walk(s -> s.filter(
            f -> authFilter(f.getDeclaringClass().getSuperclass())
        ).findFirst());
        try {
            return stackFrame.get().getDeclaringClass().getDeclaredMethod("auth");
        } catch (NoSuchMethodException e) {
            return null
        }
        
        
        List<StackWalker.StackFrame> stackFrames = sw.walk(frames ->  frames.limit(3).collect(Collectors.toList()));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("Bytecode Index", stackFrames.get(2).getByteCodeIndex());
        row.put("Class Name", stackFrames.get(2).getClassName());
        row.put("Declaring Class", stackFrames.get(2).getDeclaringClass());
            
        Class<?> superClass = stackFrames.get(2).getDeclaringClass().getSuperclass();
        if (superClass != null) {
            try {
                UserPrincipal upn = (UserPrincipal) superClass.getDeclaredMethod("auth").invoke();
                row.put("auth exists", upn);
            } catch (NoSuchMethodException e) {
                row.put("auth exists", "not exists");
            }
        } else {row.put("auth exists", "not exists");}
        row.put("File Name", stackFrames.get(2).getFileName());
        row.put("Method Name", stackFrames.get(2).getMethodName());
        row.put("Is Native", stackFrames.get(2).isNativeMethod());
        row.put("Line Number", stackFrames.get(2).getLineNumber());
        return row;
    }
    
    private static boolean authFilter(Class<?> superClass) {
        if (superClass == null) {return false;}
        boolean matched = false;
        for(Method m: superClass.getDeclaredMethods()) {
            final int mod = m.getModifiers(),
            access=Modifier.PUBLIC|Modifier.PROTECTED|Modifier.PRIVATE;
            if((mod&access) == Modifier.PROTECTED && m.getName().equals("auth")) {
                matched = true;
                break;
            }
        }
        return matched;
    }
    */
