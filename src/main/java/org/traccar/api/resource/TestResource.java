package org.traccar.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.DELETE;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.traccar.Context;
import org.traccar.config.Keys;
import org.traccar.BaseProtocol;
import org.traccar.api.auth.AuthResource;

import org.traccar.database.DB;
@Path("test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TestResource extends AuthResource {

    @GET
    public Response test() throws Exception {
        //return Response.ok(DB.table("devices").whereIn("name", new Object[] {"tk03_11","tk03_12"}).delete()).build();
        //String dir = org.traccar.Context.getConfig().getString(org.traccar.config.Keys.MODEL_CONF_DIR);
        //return response(OK).entity(new java.io.File(dir).listFiles()[0].getAbsolutePath()).build();
        //name=tk03_10&uniqueid=tk03_1023456789&
        //new org.traccar.api.modelconf.ModelConf().getModelConf();
        
        /*Map<String, BaseProtocol> protocolList = new LinkedHashMap<>();
        String packageName = "org.traccar.protocol";
        List<String> names = new LinkedList<>();
        String packagePath = packageName.replace('.', '/');
        URL packageUrl = getClass().getClassLoader().getResource(packagePath);

        if (packageUrl.getProtocol().equals("jar")) {
            String jarFileName = URLDecoder.decode(packageUrl.getFile(), StandardCharsets.UTF_8.name());
            try (JarFile jf = new JarFile(jarFileName.substring(5, jarFileName.indexOf("!")))) {
                Enumeration<JarEntry> jarEntries = jf.entries();
                while (jarEntries.hasMoreElements()) {
                    String entryName = jarEntries.nextElement().getName();
                    if (entryName.startsWith(packagePath) && entryName.length() > packagePath.length() + 5) {
                        names.add(entryName.substring(packagePath.length() + 1, entryName.lastIndexOf('.')));
                    }
                }
            }
        }
        for (String name : names) {
            Class<?> protocolClass = Class.forName(packageName + '.' + name);
            if (BaseProtocol.class.isAssignableFrom(protocolClass) && Context.getConfig().hasKey(
                    Keys.PROTOCOL_PORT.withPrefix(BaseProtocol.nameFromClass(protocolClass)))
               ) {
                BaseProtocol protocol = (BaseProtocol) protocolClass.getDeclaredConstructor().newInstance();
                //serverList.addAll(protocol.getServerList());
                protocolList.put(protocol.getName(), protocol);
            }
        }
        return response(OK).entity(protocolList).build();*/
        
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> position = DB.table("positions")
            .select("position_id","user_id", "device_id", "unique_id", "protocol", 
                    "latitude", "longitude", "altitude", "address", "valid", "speed", "course",
                    "accuracy", "positions.attributes", "servertime", "devicetime", "fixtime")
            .join("devices", "positions.id", "=", "devices.position_id")
            .where("unique_id", "123456789")
            .first();
        response.put("success", true);
        response.put("data", position);
        return response(OK).entity(response).build();
    }

}
