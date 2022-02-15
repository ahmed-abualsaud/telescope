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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

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

@Path("admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource extends AuthResource {

    @Path("register")
    @POST
    public Response register(Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();    
        validationValues.put("name", request.get("name"));
        validationValues.put("email", request.get("email"));
        validationValues.put("phone", request.get("phone"));
        validationString.put("name", "required");
        validationString.put("email", "unique:admins|required");
        validationString.put("phone", "unique:admins|required");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
        
            String password;
            if (request.containsKey("password") && request.get("password") != null) {
                password = request.get("password").toString();
            } else {password = request.get("phone").toString();}
            
            Hashing.HashingResult hashingResult = Hashing.createHash(password);
            request.put("password", hashingResult.getHash());
            request.put("salt", hashingResult.getSalt());
            
            Map<String, Object> admin = DB.table("admins").create(request);
            String token = JWT.encode(admin.get("id").toString(), "admin");
            Map<String, Object> data = new LinkedHashMap<>();
            
            data.put("access_token", token);
            data.put("admin", admin);
            response.put("success", true);
            response.put("data", data);
            return response(OK).entity(response).build();
            
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @Path("login")
    @POST
    public Response login(Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        String emailOrPhone = request.get("emailOrPhone").toString();
        
        if (MailUtil.isValidEmailAddress(emailOrPhone)) {
            validationValues.put("email", emailOrPhone);
            validationString.put("email", "exists:admins");
        } else {
            validationValues.put("phone", emailOrPhone);
            validationString.put("phone", "exists:admins");
        }
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            validationValues.put("password", request.get("password"));
            Map<String, Object> admin = Auth.attempt(validationValues, "admins");
            if (admin == null) {
                response.put("success", false);
                response.put("errors", new String[] {"Invalid Email or Password."});
                return response(BAD_REQUEST).entity(response).build();
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("access_token", admin.remove("token"));
            data.put("admin", admin);
            response.put("success", true);
            response.put("data", data);
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @GET
    public Response get() {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> admin = DB.table("admins").find(auth().getUserId());
        response.put("success", true);
        response.put("data", admin);
        return response(OK).entity(response).build();
    }
    
    @PUT
    public Response update(Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("email", request.get("email"));
        validationValues.put("phone", request.get("phone"));
        validationString.put("email", "unique:admins");
        validationString.put("phone", "unique:admins");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            List<Map<String, Object>> admin = DB.table("admins").where("id", auth().getUserId()).update(request);
            response.put("success", true);
            response.put("data", admin.get(0));
            return response(OK).entity(response).build();
            
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @DELETE
    public Response destroy() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", DB.table("admins").where("id", auth().getUserId()).delete());
        return response(OK).entity(response).build();
    }
    
//================================================================================================================================================
    
    @Path("device/id/{device_id}")
    @GET
    public Response getDeviceById(@PathParam("device_id") long device_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> device = DB.table("devices").find(device_id);
        if (device == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Device with ID: " + device_id + " does not exists."});
            return response(BAD_REQUEST).entity(response).build();
        }
        response.put("success", true);
        response.put("data", device);
        return response(OK).entity(response).build();
    }
    
    @Path("device/unique/id/{unique_id}")
    @GET
    public Response getDeviceByUniqueId(@PathParam("unique_id") String unique_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> device = DB.table("devices").where("unique_id", unique_id).first();
        if (device == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Device with Unique ID: " + unique_id + " does not exists."});
            return response(BAD_REQUEST).entity(response).build();
        }
        response.put("success", true);
        response.put("data", device);
        return response(OK).entity(response).build();
    }
    
    @Path("user/{user_id}/devices")
    @GET
    public Response getUserDevices(@PathParam("user_id") long user_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("user_id", user_id);
        validationString.put("user_id", "exists:users.id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            response.put("success", true);
            response.put("data", DB.table("devices").where("user_id", user_id).get());
            return Response.ok(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }   
    }

    @Path("devices")
    @GET
    public Response getAllDevices() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", DB.table("devices").get());
        return Response.ok(response).build();
    }

    @Path("add/device/to/user/{user_id}")
    @POST
    public Response addDeviceToUser(@PathParam("user_id") long user_id, Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("user_id", user_id);
        validationValues.put("unique_id", request.get("unique_id"));
        validationValues.put("phone", request.get("phone"));
        validationValues.put("license_plate", request.get("license_plate"));
        validationValues.put("license_exp_date", request.get("license_exp_date"));

        validationString.put("user_id", "exists:users.id");
        validationString.put("unique_id", "unique:devices|required");
        validationString.put("phone", "unique:devices");
        validationString.put("license_plate", "unique:devices|required");
        validationString.put("license_exp_date", "required");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            request.put("user_id", user_id);
            response.put("success", true);
            response.put("data", DB.table("devices").create(request));
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @Path("update/device/{device_id}")
    @PUT
    public Response updateUserDevice(@PathParam("device_id") long device_id, Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        
        validationValues.put("device_id", device_id);
        validationValues.put("user_id", request.get("user_id"));
        validationValues.put("unique_id", request.get("unique_id"));
        validationValues.put("phone", request.get("phone"));
        validationValues.put("license_plate", request.get("license_plate"));

        validationString.put("device_id", "exists:devices.id");
        validationString.put("user_id", "exists:users.id");
        validationString.put("unique_id", "unique:devices");
        validationString.put("phone", "unique:devices");
        validationString.put("license_plate", "unique:devices");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            List<Map<String, Object>> devices = DB.table("devices").where("id", device_id).update(request);
            response.put("success", true);
            response.put("data", devices.get(0));
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @Path("device/id/{device_id}")
    @DELETE
    public Response destroyDeviceById(@PathParam("device_id") long device_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", DB.table("devices").where("id", device_id).delete());
        return response(OK).entity(response).build();
    }
    
    @Path("device/unique/id/{unique_id}")
    @DELETE
    public Response destroyDeviceByUniqueId(@PathParam("unique_id") String unique_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", DB.table("devices").where("unique_id", unique_id).delete());
        return response(OK).entity(response).build();
    }

//================================================================================================================================================

    @Path("driver/id/{driver_id}")
    @GET
    public Response getDriverById(@PathParam("driver_id") long driver_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> driver = DB.table("drivers").find(driver_id);
        if (driver == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Driver with ID: " + driver_id + " does not exists."});
            return response(BAD_REQUEST).entity(response).build();
        }
        response.put("success", true);
        response.put("data", driver);
        return response(OK).entity(response).build();
    }
    
    @Path("user/{user_id}/drivers")
    @GET
    public Response getUserDrivers(@PathParam("user_id") long user_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        
        validationValues.put("user_id", user_id);
        validationString.put("user_id", "exists:users.id");
        Validator validator = validate(validationValues, validationString);
        
        if (validator.validated()) {
            response.put("success", true);
            response.put("data", DB.table("drivers").where("user_id", user_id).get());
            return Response.ok(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @Path("drivers")
    @GET
    public Response getAllDrivers() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", DB.table("drivers").get());
        return Response.ok(response).build();
    }
    
    @Path("add/driver/to/user/{user_id}")
    @POST
    public Response addDriver(@PathParam("user_id") long user_id, Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }

        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("user_id", user_id);        
        validationValues.put("name", request.get("name"));
        validationValues.put("email", request.get("email"));
        validationValues.put("phone", request.get("phone"));

        validationString.put("user_id", "exists:users.id");        
        validationString.put("name", "required");
        validationString.put("email", "unique:drivers|required");
        validationString.put("phone", "unique:drivers|required");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
        
            String password;
            if (request.containsKey("password") && request.get("password") != null) {
                password = request.get("password").toString();
            } else {password = request.get("phone").toString();}
            
            Hashing.HashingResult hashingResult = Hashing.createHash(password);
            request.put("password", hashingResult.getHash());
            request.put("salt", hashingResult.getSalt());
            request.put("user_id", user_id);
            
            Map<String, Object> driver = DB.table("drivers").create(request);
            String token = JWT.encode(driver.get("id").toString(), "driver");
            Map<String, Object> data = new LinkedHashMap<>();
            
            data.put("access_token", token);
            data.put("driver", driver);
            response.put("success", true);
            response.put("data", data);
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @Path("update/driver/{driver_id}")
    @PUT
    public Response updateUserDriver(@PathParam("driver_id") long driver_id, Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("driver_id", driver_id);
        validationValues.put("user_id", request.get("user_id"));
        validationValues.put("email", request.get("email"));
        validationValues.put("phone", request.get("phone"));

        validationString.put("driver_id", "exists:drivers.id");
        validationString.put("user_id", "exists:users.id");
        validationString.put("email", "unique:drivers");
        validationString.put("phone", "unique:drivers");

        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            List<Map<String, Object>> drivers = DB.table("drivers").where("id", driver_id).update(request);
            response.put("success", true);
            response.put("data", drivers.get(0));
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @Path("driver/id/{driver_id}")
    @DELETE
    public Response destroyDriverById(@PathParam("driver_id") long driver_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", DB.table("drivers").where("id", driver_id).delete());
        return response(OK).entity(response).build();
    }
    
//================================================================================================================================================
    
    @Path("get/last/position/{unique_id}")
    @GET
    public Response getLastPosition(@PathParam("unique_id") String unique_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("unique_id", unique_id);
        validationString.put("unique_id", "exists:devices");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> position = DB.table("positions")
            .select("position_id","user_id", "device_id", "unique_id", "protocol", 
                    "latitude", "longitude", "altitude", "address", "valid", "speed", "course",
                    "accuracy", "positions.attributes", "servertime", "devicetime", "fixtime")
            .join("devices", "positions.id", "=", "devices.position_id")
            .where("unique_id", unique_id)
            .first();
            response.put("success", true);
            response.put("data", position);
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
}
