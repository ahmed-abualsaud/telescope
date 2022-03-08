package org.telescope.app.resource;

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

import org.telescope.javel.framework.auth.Auth;
import org.telescope.javel.framework.auth.JWT;
import org.telescope.javel.framework.helper.Hashing;
import org.telescope.javel.framework.helper.MailUtil;
import org.telescope.javel.framework.resource.Resource;
import org.telescope.javel.framework.storage.database.DB;
import org.telescope.javel.framework.validator.Validator;

import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;

@Path("driver")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DriverResource extends Resource {

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

        validationValues.put("user_id", request.get("user_id"));        
        validationValues.put("name", request.get("name"));
        validationValues.put("email", request.get("email"));
        validationValues.put("phone", request.get("phone"));
        
        validationString.put("user_id", "exists:users.id|required");        
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
            validationString.put("email", "exists:drivers");
        } else {
            validationValues.put("phone", emailOrPhone);
            validationString.put("phone", "exists:drivers");
        }
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            validationValues.put("password", request.get("password"));
            Map<String, Object> driver = Auth.attempt(validationValues, "drivers");
            if (driver == null) {
                response.put("success", false);
                response.put("errors", new String[] {"Invalid Email or Password"});
                return response(BAD_REQUEST).entity(response).build();
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("access_token", driver.remove("token"));
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
    
    @GET
    public Response get() {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> driver = DB.table("drivers").find(auth().getUserId());
        response.put("success", true);
        response.put("data", driver);
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
        validationString.put("email", "unique:drivers");
        validationString.put("phone", "unique:drivers");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
        
            List<Map<String, Object>> driver = DB.table("drivers").where("id", auth().getUserId()).update(request);
            response.put("success", true);
            response.put("data", driver.get(0));
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
        response.put("success", DB.table("drivers").where("id", auth().getUserId()).delete());
        return response(OK).entity(response).build();
    }
}
