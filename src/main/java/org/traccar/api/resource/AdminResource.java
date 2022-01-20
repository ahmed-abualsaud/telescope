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

@Path("admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource extends AuthResource {

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
            validationString.put("email", "exists:admins");
        } else {
            validationValues.put("phone", emailOrPhone);
            validationString.put("phone", "exists:admins");
        }
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
        
            Map<String, Object> data = new LinkedHashMap<>();
            validationValues.put("password", request.get("password"));
            Map<String, Object> admin = Auth.attempt(validationValues, "admins");
            
            if (admin == null) {
                data.put("message", "Invalid Email or Password");
                response.put("success", false);
                response.put("error", data);
                return response(BAD_REQUEST).entity(response).build();
            }
            
            data.put("access_token", admin.remove("token"));
            data.put("admin", admin);
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
        Map<String, Object> admin = DB.table("admins").where("id", auth().getUserId()).first();
        response.put("success", true);
        response.put("data", admin);
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
        
        validationString.put("email", "unique:admins");
        validationString.put("phone", "unique:admins");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
        
            List<Map<String, Object>> admin = DB.table("admins").where("id", auth().getUserId()).update(request);
            response.put("success", true);
            response.put("data", admin);
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
        response.put("success", DB.table("admins").where("id", auth().getUserId()).delete());
        return response(OK).entity(response).build();
    }
}