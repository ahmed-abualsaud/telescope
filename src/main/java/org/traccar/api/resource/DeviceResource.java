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
import java.sql.SQLException;

import org.traccar.api.auth.JWT;
import org.traccar.api.auth.Auth;
import org.traccar.api.auth.AuthResource;
import org.traccar.api.validator.Validator;
import org.traccar.database.DB;
import org.traccar.helper.Hashing;
import org.traccar.helper.MailUtil;

import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;

@Path("devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceResource extends AuthResource {

    
    @Path("user/device/{uniqueid}")
    @GET
    public void get(@PathParam("partnerId") long partnerId, 
    		@QueryParam("uniqueId") List<String> uniqueIds) throws SQLException {
    		
        /*Map<String, Object> request = new LinkedHashMap<>();
        request.put("partnerId", partnerId);
        request.put("uniqueId", uniqueIds);
        
        Map<String, String> validationString = new LinkedHashMap<>();
        validationString.put("partnerId", "exists:user.id");
        validationString.put("uniqueId", "exists:device|multiple");
        
        Map<String, Object> response = new LinkedHashMap<>();
        Validator validator = Validator.validate(request, validationString);
        if (validator.validated()) {
        
            DeviceManager deviceManager = Context.getDeviceManager();
            Set<Long> result;
            if (uniqueIds.isEmpty()) {
                result = deviceManager.getUserItems(partnerId);
            } else {
                result = new HashSet<>();
                for (String uniqueId : uniqueIds) {
                    Device device = deviceManager.getByUniqueId(uniqueId);
                    Context.getPermissionsManager().checkDevice(partnerId, device.getId());
                    result.add(device.getId());
                }
            }
            response.put("success", true);
            response.put("data", deviceManager.getItems(result));
            return Response.ok(response).build();
            
        } else {
            response.put("success", false);
            response.put("error", validator.getErrors());
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }*/
    }
    
    @Path("partner/{partnerId}")
    @PUT
    public void update(Map<String, Object> entity, @PathParam("partnerId") long partnerId) throws SQLException {
    
        /*Map<String, Object> request = new LinkedHashMap<>();
        request.put("partnerId", partnerId);
        request.put("uniqueId", entity.getUniqueId());
        
        Map<String, String> validationString = new LinkedHashMap<>();
        validationString.put("partnerId", "exists:user.id");
        validationString.put("uniqueId", "exists:device|required");
        
        Map<String, Object> response = new LinkedHashMap<>();
        Validator validator = Validator.validate(request, validationString);
        if (validator.validated()) {
        
            Context.getPermissionsManager().checkDeviceReadonly(partnerId);
            DeviceManager deviceManager = Context.getDeviceManager();
            long id = deviceManager.getByUniqueId(entity.getUniqueId()).getId();
            entity.setId(id);
            Context.getPermissionsManager().checkDevice(partnerId, entity.getId());
            deviceManager.updateItem(entity);
            LogAction.edit(partnerId, entity);
            Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
            Context.getPermissionsManager().refreshAllExtendedPermissions();
            response.put("success", true);
            response.put("data", entity);
            return Response.ok(response).build();
            
        } else {
            response.put("success", false);
            response.put("error", validator.getErrors());
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }*/
    }
    
    @Path("partner/{partnerId}")
    @DELETE
    public void remove(@PathParam("partnerId") long partnerId, 
    		@QueryParam("uniqueId") List<String> uniqueIds) throws SQLException {
    		
       /* Map<String, Object> request = new LinkedHashMap<>();
        request.put("partnerId", partnerId);
        request.put("uniqueId", uniqueIds);
        
        Map<String, String> validationString = new LinkedHashMap<>();
        validationString.put("partnerId", "exists:user.id");
        validationString.put("uniqueId", "exists:device|multiple|required");
        
        Map<String, Object> response = new LinkedHashMap<>();
        Validator validator = Validator.validate(request, validationString);
        if (validator.validated()) {
        
            Context.getPermissionsManager().checkReadonly(partnerId);
            Context.getPermissionsManager().checkDeviceReadonly(partnerId);
            DeviceManager deviceManager = Context.getDeviceManager();
            long id;
            for (String uniqueId : uniqueIds) {
                id = deviceManager.getByUniqueId(uniqueId).getId();
                Context.getPermissionsManager().checkDevice(partnerId, id);
                deviceManager.removeItem(id);
                LogAction.remove(partnerId, Device.class, id);
            }
            Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
            Context.getPermissionsManager().refreshAllExtendedPermissions();
            response.put("success", true);
            response.put("data", "Devices Deleted Successfully");
            return Response.ok(response).build();
            
        } else {
            response.put("success", false);
            response.put("error", validator.getErrors());
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }*/
    }
}
