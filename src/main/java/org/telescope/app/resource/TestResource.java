package org.telescope.app.resource;

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

import org.telescope.config.Keys;
import org.telescope.javel.framework.resource.Resource;
import org.telescope.javel.framework.storage.database.DB;
import org.telescope.javel.framework.storage.database.Eloquent;
import org.telescope.server.BaseProtocol;
import org.telescope.server.Context;

@Path("test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TestResource extends Resource {

    @GET
    public Response test() throws Exception {
        //return Response.ok(DB.table("devices").whereIn("name", new Object[] {"tk03_11","tk03_12"}).delete()).build();
        //String dir = org.telescope.Context.getConfig().getString(org.telescope.config.Keys.MODEL_CONF_DIR);
        //return response(OK).entity(new java.io.File(dir).listFiles()[0].getAbsolutePath()).build();
        //name=tk03_10&uniqueid=tk03_1023456789&
        //new org.telescope.api.modelconf.ModelConf().getModelConf();
        
        /*Map<String, BaseProtocol> protocolList = new LinkedHashMap<>();
        String packageName = "org.telescope.protocol";
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
        return response(OK).entity(packageUrl.toString()).build();*/
        
        /*Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> position = DB.table("positions")
            .select("position_id","user_id", "device_id", "unique_id", "protocol", 
                    "latitude", "longitude", "altitude", "address", "valid", "speed", "course",
                    "accuracy", "positions.attributes", "servertime", "devicetime", "fixtime")
            .join("devices", "positions.id", "=", "devices.position_id")
            .where("unique_id", "123456789")
            .first();
        response.put("success", true);
        response.put("data", position);
        return response(OK).entity(response).build();*/
        
        Eloquent eloquent = DB.table("devices")
            .select("positions.id as position_id","user_id", "device_id", "unique_id", 
                    "alarm", "latitude", "longitude", "altitude", "valid", "motion", 
                    "address", "speed->val as speed", "acceleration->val as acceleration", 
                    "duration->val as duration", "distance->val as distance",
                    "engine->status as engine_status", "engine->fuel_level as fuel_level",
                    "course", "accuracy", "positions.attributes", "battery",
                    "device_time", "fix_time", "devices.created_at")
            .join("positions", "devices.id", "=", "positions.device_id")
            .join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", 2)
            .where("devices.id", 1);
            
        return response(OK).entity(eloquent.last()).build();
    }

}
