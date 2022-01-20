package org.traccar.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.DELETE;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
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

@Path("user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource extends AuthResource {

    @Path("register")
    @POST
    public Response register(Map<String, Object> request) throws SQLException {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            Map<String, String> message = new LinkedHashMap<>();
            message.put("message", "Please set input data");
            response.put("error", message);
            return response(BAD_REQUEST).entity(response).build();
        }

        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("name", request.get("name"));
        validationValues.put("email", request.get("email"));
        validationValues.put("phone", request.get("phone"));
        
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
            response.put("error", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("login")
    @POST
    public Response login(Map<String, Object> request) throws SQLException {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            Map<String, String> message = new LinkedHashMap<>();
            message.put("message", "Please set input data");
            response.put("error", message);
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
        
            Map<String, Object> data = new LinkedHashMap<>();
            validationValues.put("password", request.get("password"));
            Map<String, Object> user = Auth.attempt(validationValues, "users");
            
            if (user == null) {
                data.put("message", "Invalid Email or Password");
                response.put("success", false);
                response.put("error", data);
                return response(BAD_REQUEST).entity(response).build();
            }
            
            data.put("access_token", user.remove("token"));
            data.put("user", user);
            response.put("success", true);
            response.put("data", data);
            return response(OK).entity(response).build();
            
        } else {
            response.put("success", false);
            response.put("error", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @GET
    public Response get() throws SQLException {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> user = DB.table("users").where("id", auth().getUserId()).first();
        response.put("success", true);
        response.put("data", user);
        return response(OK).entity(response).build();
    }
    
    @PUT
    public Response update(Map<String, Object> request) throws SQLException {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            Map<String, String> message = new LinkedHashMap<>();
            message.put("message", "Please set input data");
            response.put("error", message);
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
            response.put("data", user);
            return response(OK).entity(response).build();
            
        } else {
            response.put("success", false);
            response.put("error", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @DELETE
    public Response destroy() throws SQLException {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", DB.table("users").where("id", auth().getUserId()).delete());
        return response(OK).entity(response).build();
    }
    
    /*@Path("test")
    @GET
    public Response test(
        @QueryParam("name") String name,
        @QueryParam("uniqueid") String uniqueid,
        @QueryParam("partnerid") long partnerid
    ) {
        //return Response.ok(DB.table("devices").whereIn("name", new Object[] {"tk03_11","tk03_12"}).delete()).build();
        return response(OK).entity("hi").build();
        //name=tk03_10&uniqueid=tk03_1023456789&
    }*/
    
}
