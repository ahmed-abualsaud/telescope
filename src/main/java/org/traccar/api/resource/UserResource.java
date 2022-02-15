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

@Path("user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource extends AuthResource {

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

        validationValues.put("partner_id", request.get("partner_id"));        
        validationValues.put("name", request.get("name"));
        validationValues.put("email", request.get("email"));
        validationValues.put("phone", request.get("phone"));

        validationString.put("partner_id", "exists:partners.id|required");        
        validationString.put("name", "required");
        validationString.put("email", "unique:users|required");
        validationString.put("phone", "unique:users|required");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            String password;
            if (request.containsKey("password") && request.get("password") != null) {
                password = request.get("password").toString();
            } else {password = request.get("phone").toString();}
            Hashing.HashingResult hashingResult = Hashing.createHash(password);
            request.put("password", hashingResult.getHash());
            request.put("salt", hashingResult.getSalt());
            
            Map<String, Object> user = DB.table("users").create(request);
            String token = JWT.encode(user.get("id").toString(), "user");
            Map<String, Object> data = new LinkedHashMap<>();
            
            data.put("access_token", token);
            data.put("user", user);
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
            validationString.put("email", "exists:users");
        } else {
            validationValues.put("phone", emailOrPhone);
            validationString.put("phone", "exists:users");
        }
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            validationValues.put("password", request.get("password"));
            Map<String, Object> user = Auth.attempt(validationValues, "users");
            if (user == null) {
                response.put("success", false);
                response.put("errors", new String[] {"Invalid Email or Password"});
                return response(BAD_REQUEST).entity(response).build();
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("access_token", user.remove("token"));
            data.put("user", user);
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
        Map<String, Object> user = DB.table("users").find(auth().getUserId());
        response.put("success", true);
        response.put("data", user);
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
        validationString.put("email", "unique:users");
        validationString.put("phone", "unique:users");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            List<Map<String, Object>> user = DB.table("users").where("id", auth().getUserId()).update(request);
            response.put("success", true);
            response.put("data", user.get(0));
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
        response.put("success", DB.table("users").where("id", auth().getUserId()).delete());
        return response(OK).entity(response).build();
    }

//================================================================================================================================================

    @Path("device/id/{device_id}")
    @GET
    public Response getDeviceById(@PathParam("device_id") long device_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> device = DB.table("devices")
        .where("user_id", auth().getUserId()).where("id", device_id).first();
        if (device == null) {
            response.put("success", false);
            response.put("errors", new String[] {"You have no access to device with ID: " + device_id});
            return response(OK).entity(response).build();
        }
        response.put("success", true);
        response.put("data", device);
        return response(OK).entity(response).build();
    }
    
    @Path("device/unique/id/{unique_id}")
    @GET
    public Response getDeviceByUniqueId(@PathParam("unique_id") String unique_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> device = DB.table("devices")
        .where("user_id", auth().getUserId()).where("unique_id", unique_id).first();
        if (device == null) {
            response.put("success", false);
            response.put("errors", new String[] {"You have no access to device with Unique ID: " + unique_id});
            return response(OK).entity(response).build();
        }
        response.put("success", true);
        response.put("data", device);
        return response(OK).entity(response).build();
    }
    
    @Path("devices")
    @GET
    public Response getDevices() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", DB.table("devices").where("user_id", auth().getUserId()).get());
        return Response.ok(response).build();
    }
    
    @Path("device")
    @POST
    public Response addDevice(Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }

        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("unique_id", request.get("unique_id"));
        validationValues.put("phone", request.get("phone"));
        validationValues.put("license_plate", request.get("license_plate"));
        validationValues.put("license_exp_date", request.get("license_exp_date"));
        
        validationString.put("unique_id", "unique:devices|required");
        validationString.put("phone", "unique:devices");
        validationString.put("license_plate", "unique:devices|required");
        validationString.put("license_exp_date", "required");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
        
            request.put("user_id", auth().getUserId());
            response.put("success", true);
            response.put("data", DB.table("devices").create(request));
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @Path("device/id/{device_id}")
    @PUT
    public Response updateDevice(@PathParam("device_id") long device_id, Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("device_id", device_id);
        validationValues.put("unique_id", request.get("unique_id"));
        validationValues.put("phone", request.get("phone"));
        validationValues.put("license_plate", request.get("license_plate"));
        
        validationString.put("device_id", "exists:devices.id");
        validationString.put("unique_id", "unique:devices");
        validationString.put("phone", "unique:devices");
        validationString.put("license_plate", "unique:devices");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            List<Map<String, Object>> devices = DB.table("devices").where("user_id", auth().getUserId())
            .where("id", device_id).update(request);
            if (devices == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with ID: " + device_id});
                return response(OK).entity(response).build();
            }
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
        response.put("success", DB.table("devices").where("user_id", auth()
        .getUserId()).where("id", device_id).delete());
        return response(OK).entity(response).build();
    }
    
    @Path("device/unique/id/{unique_id}")
    @DELETE
    public Response destroyDeviceByUniqueId(@PathParam("unique_id") String unique_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", DB.table("devices").where("user_id", auth()
        .getUserId()).where("unique_id", unique_id).delete());
        return response(OK).entity(response).build();
    }

//================================================================================================================================================

    @Path("driver/id/{driver_id}")
    @GET
    public Response getDriver(@PathParam("driver_id") long driver_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> driver = DB.table("drivers")
        .where("user_id", auth().getUserId()).where("id", driver_id).first();
        if (driver == null) {
            response.put("success", false);
            response.put("errors", new String[] {"You have no access to driver with ID: " + driver_id});
            return response(OK).entity(response).build();
        }
        response.put("success", true);
        response.put("data", driver);
        return response(OK).entity(response).build();
    }
    
    @Path("drivers")
    @GET
    public Response getDrivers() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", DB.table("drivers").where("user_id", auth().getUserId()).get());
        return Response.ok(response).build();
    }
    
    @Path("driver")
    @POST
    public Response addDriver(Map<String, Object> request) {
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
            request.put("user_id", auth().getUserId());
            
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
    
    @Path("driver/id/{driver_id}")
    @PUT
    public Response updateDriver(@PathParam("driver_id") long driver_id, Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("driver_id", driver_id);
        validationValues.put("email", request.get("email"));
        validationValues.put("phone", request.get("phone"));
        validationString.put("driver_id", "exists:drivers.id");
        validationString.put("email", "unique:drivers");
        validationString.put("phone", "unique:drivers");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            if (request.containsKey("password") && request.get("password") != null) {
                String password = request.get("password").toString();
                Hashing.HashingResult hashingResult = Hashing.createHash(password);
                request.put("password", hashingResult.getHash());
                request.put("salt", hashingResult.getSalt());
            }
            List<Map<String, Object>> drivers = DB.table("drivers").where("user_id", auth().getUserId())
            .where("id", driver_id).update(request);
            if (drivers == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to driver with ID: " + driver_id});
                return response(OK).entity(response).build();
            }
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
    public Response destroyDriver(@PathParam("driver_id") long driver_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", DB.table("drivers").where("user_id", auth()
        .getUserId()).where("id", driver_id).delete());
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
            .where("user_id", auth().getUserId())
            .where("unique_id", unique_id)
            .first();
            
            if (position == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with Unique ID: " + unique_id});
                return response(BAD_REQUEST).entity(response).build();
            }
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
