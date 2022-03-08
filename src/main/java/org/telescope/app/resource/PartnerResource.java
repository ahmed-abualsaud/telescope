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

import org.telescope.app.model.Position;
import org.telescope.app.model.Maintenance;
import org.telescope.javel.framework.auth.Auth;
import org.telescope.javel.framework.auth.JWT;
import org.telescope.javel.framework.helper.Hashing;
import org.telescope.javel.framework.helper.MailUtil;
import org.telescope.javel.framework.resource.Resource;
import org.telescope.javel.framework.storage.database.DB;
import org.telescope.javel.framework.storage.database.Eloquent;
import org.telescope.javel.framework.validator.Validator;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Path("partner")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PartnerResource extends Resource {

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
        validationString.put("email", "unique:partners|required");
        validationString.put("phone", "unique:partners|required");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            String password;
            if (request.containsKey("password") && request.get("password") != null) {
                password = request.get("password").toString();
            } else {password = request.get("phone").toString();}
            Hashing.HashingResult hashingResult = Hashing.createHash(password);
            request.put("password", hashingResult.getHash());
            request.put("salt", hashingResult.getSalt());
            
            Map<String, Object> partner = DB.table("partners").create(request);
            String token = JWT.encode(partner.get("id").toString(), "partner");
            Map<String, Object> data = new LinkedHashMap<>();
            
            data.put("access_token", token);
            data.put("partner", partner);
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
            validationString.put("email", "exists:partners");
        } else {
            validationValues.put("phone", emailOrPhone);
            validationString.put("phone", "exists:partners");
        }
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            validationValues.put("password", request.get("password"));
            Map<String, Object> partner = Auth.attempt(validationValues, "partners");
            if (partner == null) {
                response.put("success", false);
                response.put("errors", new String[] {"Invalid Email or Password"});
                return response(BAD_REQUEST).entity(response).build();
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("access_token", partner.remove("token"));
            data.put("partner", partner);
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
        Map<String, Object> partner = DB.table("partners").find(auth().getUserId());
        response.put("success", true);
        response.put("data", partner);
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
        validationString.put("email", "unique:partners");
        validationString.put("phone", "unique:partners");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            List<Map<String, Object>> partner = DB.table("partners").where("id", auth().getUserId()).update(request);
            response.put("success", true);
            response.put("data", partner.get(0));
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
        response.put("success", DB.table("partners").where("id", auth().getUserId()).delete());
        return response(OK).entity(response).build();
    }

//================================================================================================================================================

    @Path("user/id/{user_id}")
    @GET
    public Response getUserById(@PathParam("user_id") long user_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> user = DB.table("users")
        .where("partner_id", auth().getUserId()).where("id", user_id).first();
        if (user == null) {
            response.put("success", false);
            response.put("errors", new String[] {"You have no access to user with ID: " + user_id});
            return response(OK).entity(response).build();
        }
        response.put("success", true);
        response.put("data", user);
        return response(OK).entity(response).build();
    }

    @Path("user/public/id/{public_id}")
    @GET
    public Response getUserByPublicId(@PathParam("public_id") long public_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> user = DB.table("users")
        .where("partner_id", auth().getUserId()).where("public_id", public_id).first();
        if (user == null) {
            response.put("success", false);
            response.put("errors", new String[] {"You have no access to user with public ID: " + public_id});
            return response(OK).entity(response).build();
        }
        response.put("success", true);
        response.put("data", user);
        return response(OK).entity(response).build();
    }

    @Path("users")
    @GET
    public Response getUsers() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", DB.table("users").where("partner_id", auth().getUserId()).get());
        return Response.ok(response).build();
    }

    @Path("user")
    @POST
    public Response addUser(Map<String, Object> request) {
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
            request.put("partner_id", auth().getUserId());
            
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

    @Path("user/id/{user_id}")
    @PUT
    public Response updateUserById(@PathParam("user_id") long user_id, Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("user_id", user_id);
        validationValues.put("email", request.get("email"));
        validationValues.put("phone", request.get("phone"));
        validationString.put("user_id", "exists:users.id");
        validationString.put("email", "unique:users");
        validationString.put("phone", "unique:users");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            List<Map<String, Object>> users = DB.table("users").where("partner_id", auth().getUserId())
            .where("id", user_id).update(request);
            if (users == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to user with ID: " + user_id});
                return response(OK).entity(response).build();
            }
            response.put("success", true);
            response.put("data", users.get(0));
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("user/public/id/{public_id}")
    @PUT
    public Response updateUserByPublicId(@PathParam("public_id") long public_id, Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("public_id", public_id);
        validationValues.put("email", request.get("email"));
        validationValues.put("phone", request.get("phone"));
        validationString.put("public_id", "exists:users");
        validationString.put("email", "unique:users");
        validationString.put("phone", "unique:users");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            List<Map<String, Object>> users = DB.table("users").where("partner_id", auth().getUserId())
            .where("public_id", public_id).update(request);
            if (users == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to user with ID: " + public_id});
                return response(OK).entity(response).build();
            }
            response.put("success", true);
            response.put("data", users.get(0));
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("user/id/{user_id}")
    @DELETE
    public Response destroyUserById(@PathParam("user_id") long user_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("user_id", user_id);
        validationString.put("user_id", "exists:users.id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            response.put("success", DB.table("users").where("partner_id", auth()
            .getUserId()).where("id", user_id).delete());
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("user/public/id/{public_id}")
    @DELETE
    public Response destroyUserByPublicId(@PathParam("public_id") long public_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("public_id", public_id);
        validationString.put("public_id", "exists:users");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            response.put("success", DB.table("users").where("partner_id", auth()
            .getUserId()).where("public_id", public_id).delete());
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

//================================================================================================================================================

    @Path("device/id/{device_id}")
    @GET
    public Response getDeviceByDeviceId(@PathParam("device_id") long device_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("device_id", device_id);
        validationString.put("device_id", "exists:devices.id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> device = DB.table("devices")
            .select("devices.id as device_id", "devices.name as device_name", "devices.phone as device_phone",
                    "devices.avatar as device_avatar", "license_plate", "license_exp_date",
                    "unique_id","user_id", "users.name as user_name", "users.email as user_email", 
                    "users.phone as user_phone", "users.avatar as user_avatar", "users.public_id",
                    "position_id", "devices.attributes", "model", "contact",  "devices.disabled", 
                    "devices.updated_at", "devices.created_at")
            .join("users", "users.id", "=", "devices.user_id")
            .where("partner_id", auth().getUserId())
            .where("devices.id", device_id).first();
            if (device == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with ID: " + device_id});
                return response(OK).entity(response).build();
            }
            response.put("success", true);
            response.put("data", device);
            return Response.ok(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }   
    }

    @Path("device/unique/id/{unique_id}")
    @GET
    public Response getDeviceByDeviceUniqueId(@PathParam("unique_id") String unique_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("unique_id", unique_id);
        validationString.put("unique_id", "exists:devices");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> device = DB.table("devices")
            .select("devices.id as device_id", "devices.name as device_name", "devices.phone as device_phone",
                    "devices.avatar as device_avatar", "license_plate", "license_exp_date",
                    "unique_id","user_id", "users.name as user_name", "users.email as user_email", 
                    "users.phone as user_phone", "users.avatar as user_avatar", "users.public_id",
                    "position_id", "devices.attributes", "model", "contact",  "devices.disabled", 
                    "devices.updated_at", "devices.created_at")
            .join("users", "users.id", "=", "devices.user_id")
            .where("partner_id", auth().getUserId())
            .where("devices.unique_id", unique_id).first();
            if (device == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with unique ID: " + unique_id});
                return response(OK).entity(response).build();
            }
            response.put("success", true);
            response.put("data", device);
            return Response.ok(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }   
    }

    @Path("user/{user_id}/devices")
    @GET
    public Response getUserDevicesByUserId(@PathParam("user_id") long user_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("user_id", user_id);
        validationString.put("user_id", "exists:users.id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            List<Map<String, Object>> devices = DB.table("devices")
            .select("devices.id as device_id", "devices.name as device_name", "devices.phone as device_phone",
                    "devices.avatar as device_avatar", "license_plate", "license_exp_date",
                    "unique_id","user_id", "users.name as user_name", "users.email as user_email", 
                    "users.phone as user_phone", "users.avatar as user_avatar", "users.public_id",
                    "position_id", "devices.attributes", "model", "contact",  "devices.disabled", 
                    "devices.updated_at", "devices.created_at")
            .join("users", "users.id", "=", "devices.user_id")
            .where("partner_id", auth().getUserId())
            .where("user_id", user_id)
            .get();
            if (devices == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to user with ID: " + user_id});
                return response(OK).entity(response).build();
            }
            response.put("success", true);
            response.put("data", devices);
            return Response.ok(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }   
    }

    @Path("public/user/{public_id}/devices")
    @GET
    public Response getUserDevicesByUserPublicId(@PathParam("public_id") long public_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("public_id", public_id);
        validationString.put("public_id", "exists:users");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            List<Map<String, Object>> devices = DB.table("devices")
            .select("devices.id as device_id", "devices.name as device_name", "devices.phone as device_phone",
                    "devices.avatar as device_avatar", "license_plate", "license_exp_date",
                    "unique_id","user_id", "users.name as user_name", "users.email as user_email", 
                    "users.phone as user_phone", "users.avatar as user_avatar", "users.public_id",
                    "position_id", "devices.attributes", "model", "contact",  "devices.disabled", 
                    "devices.updated_at", "devices.created_at")
            .join("users", "users.id", "=", "devices.user_id")
            .where("partner_id", auth().getUserId())
            .where("public_id", public_id)
            .get();
            if (devices == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to user with public ID: " + public_id});
                return response(OK).entity(response).build();
            }
            response.put("success", true);
            response.put("data", devices);
            return Response.ok(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }   
    }

    @Path("devices")
    @GET
    public Response getDevices() {
        Map<String, Object> response = new LinkedHashMap<>();
        List<Map<String, Object>> devices = DB.table("devices")
        .select("devices.id as device_id", "devices.name as device_name", "devices.phone as device_phone",
                "devices.avatar as device_avatar", "license_plate", "license_exp_date",
                "unique_id","user_id", "users.name as user_name", "users.email as user_email", 
                "users.phone as user_phone", "users.avatar as user_avatar", "users.public_id",
                "position_id", "devices.attributes", "model", "contact",  "devices.disabled", 
                "devices.updated_at", "devices.created_at")
        .join("users", "users.id", "=", "devices.user_id")
        .where("partner_id", auth().getUserId())
        .get();
        if (devices == null) {
            response.put("success", false);
            response.put("errors", new String[] {"You have no devices"});
            return response(OK).entity(response).build();
        }
        response.put("success", true);
        response.put("data", devices);
        return Response.ok(response).build(); 
    }

    @Path("add/device/to/user/{user_id}")
    @POST
    public Response addUserDeviceByUserId(
        @PathParam("user_id") long user_id,
        Map<String, Object> request
    ) {
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
            Map<String, Object> user = DB.table("users").where("partner_id", auth().getUserId())
            .where("id", user_id).first();
            if (user == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to user with ID: " + user_id});
                return response(OK).entity(response).build();
            }
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

    @Path("add/device/to/public/user/{public_id}")
    @POST
    public Response addUserDeviceByUserPublicId(
        @PathParam("public_id") long public_id, 
        Map<String, Object> request
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("public_id", public_id);
        validationValues.put("unique_id", request.get("unique_id"));
        validationValues.put("phone", request.get("phone"));
        validationValues.put("license_plate", request.get("license_plate"));
        validationValues.put("license_exp_date", request.get("license_exp_date"));

        validationString.put("public_id", "exists:users");
        validationString.put("unique_id", "unique:devices|required");
        validationString.put("phone", "unique:devices");
        validationString.put("license_plate", "unique:devices|required");
        validationString.put("license_exp_date", "required");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> user = DB.table("users").where("partner_id", auth().getUserId())
            .where("public_id", public_id).first();
            if (user == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to user with public ID: " + public_id});
                return response(OK).entity(response).build();
            }
            request.put("user_id", user.get("id"));
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
    public Response updateUserDeviceByDeviceId(
        @PathParam("device_id") long device_id, 
        Map<String, Object> request
    ) {
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
        validationValues.put("public_id", request.get("public_id"));
        validationValues.put("unique_id", request.get("unique_id"));
        validationValues.put("phone", request.get("phone"));
        validationValues.put("license_plate", request.get("license_plate"));

        validationString.put("device_id", "exists:devices.id");
        validationString.put("user_id", "exists:users.id");
        validationString.put("public_id", "exists:users");
        validationString.put("unique_id", "unique:devices");
        validationString.put("phone", "unique:devices");
        validationString.put("license_plate", "unique:devices");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> device = DB.table("devices")
            .join("users", "users.id", "=", "devices.user_id")
            .where("partner_id", auth().getUserId())
            .where("devices.id", device_id).first();
            if (device == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with ID: " + device_id});
                return response(OK).entity(response).build();
            }
            if (request.containsKey("user_id")) {
                Map<String, Object> user = DB.table("users").where("partner_id", auth().getUserId())
                .where("id", request.get("user_id")).first();
                if (user == null) {
                    response.put("success", false);
                    response.put("errors", new String[] {"You have no access to user with ID: " + request.get("user_id")});
                    return response(OK).entity(response).build();
                }
            }
            if (request.containsKey("public_id")) {
                Map<String, Object> user = DB.table("users").where("partner_id", auth().getUserId())
                .where("public_id", request.get("public_id")).first();
                if (user == null) {
                    response.put("success", false);
                    response.put("errors", new String[] {"You have no access to user with public ID: " + request.get("public_id")});
                    return response(OK).entity(response).build();
                }
                request.remove("public_id");
                request.put("user_id", user.get("id"));
            }
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

    @Path("device/unique/id/{unique_id}")
    @PUT
    public Response updateUserDeviceByDeviceUniqueId(
        @PathParam("unique_id") String unique_id, 
        Map<String, Object> request
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("unique_id", unique_id);
        validationValues.put("user_id", request.get("user_id"));
        validationValues.put("public_id", request.get("public_id"));
        validationValues.put("id", request.get("id"));
        validationValues.put("phone", request.get("phone"));
        validationValues.put("license_plate", request.get("license_plate"));

        validationString.put("unique_id", "exists:devices");
        validationString.put("user_id", "exists:users.id");
        validationString.put("public_id", "exists:users");
        validationString.put("id", "unique:devices");
        validationString.put("phone", "unique:devices");
        validationString.put("license_plate", "unique:devices");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> device = DB.table("devices")
            .join("users", "users.id", "=", "devices.user_id")
            .where("partner_id", auth().getUserId())
            .where("devices.unique_id", unique_id).first();
            if (device == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with unique ID: " + unique_id});
                return response(OK).entity(response).build();
            }
            if (request.containsKey("user_id")) {
                Map<String, Object> user = DB.table("users").where("partner_id", auth().getUserId())
                .where("id", request.get("user_id")).first();
                if (user == null) {
                    response.put("success", false);
                    response.put("errors", new String[] {"You have no access to user with ID: " + request.get("user_id")});
                    return response(OK).entity(response).build();
                }
            }
            if (request.containsKey("public_id")) {
                Map<String, Object> user = DB.table("users").where("partner_id", auth().getUserId())
                .where("public_id", request.get("public_id")).first();
                if (user == null) {
                    response.put("success", false);
                    response.put("errors", new String[] {"You have no access to user with public ID: " + request.get("public_id")});
                    return response(OK).entity(response).build();
                }
                request.remove("public_id");
                request.put("user_id", user.get("id"));
            }
            
            List<Map<String, Object>> devices = DB.table("devices").where("unique_id", unique_id).update(request);
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
    public Response destroyDeviceByDeviceId(@PathParam("device_id") long device_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("device_id", device_id);
        validationString.put("device_id", "exists:devices.id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
             Map<String, Object> device = DB.table("devices")
            .join("users", "users.id", "=", "devices.user_id")
            .where("partner_id", auth().getUserId())
            .where("devices.id", device_id).first();
            if (device == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with ID: " + device_id});
                return response(OK).entity(response).build();
            }
            response.put("success", DB.table("devices").where("id", device_id).delete());
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @Path("device/unique/id/{unique_id}")
    @DELETE
    public Response destroyDeviceByDeviceUniqueId(@PathParam("unique_id") String unique_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("unique_id", unique_id);
        validationString.put("unique_id", "exists:devices");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> device = DB.table("devices")
            .join("users", "users.id", "=", "devices.user_id")
            .where("partner_id", auth().getUserId())
            .where("devices.unique_id", unique_id).first();
            if (device == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with unique ID: " + unique_id});
                return response(OK).entity(response).build();
            }
            response.put("success", DB.table("devices").where("unique_id", unique_id).delete());
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

//===============================================================================================================================================

    @Path("driver/id/{driver_id}")
    @GET
    public Response getDriverByDriverId(@PathParam("driver_id") String driver_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("driver_id", driver_id);
        validationString.put("driver_id", "exists:drivers.id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> driver = DB.table("drivers")
            .select("drivers.id as driver_id", "drivers.name as driver_name", 
                    "drivers.email as driver_email", "drivers.phone as driver_phone", 
                    "drivers.avatar as driver_avatar", "user_id", "users.name as user_name", 
                    "users.email as user_email",  "users.phone as user_phone", 
                    "users.avatar as user_avatar", "users.public_id", "drivers.disabled", 
                    "drivers.updated_at", "drivers.created_at")
            .join("users", "users.id", "=", "drivers.user_id")
            .where("partner_id", auth().getUserId())
            .where("drivers.id", driver_id).first();
            if (driver == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to driver with ID: " + driver_id});
                return response(OK).entity(response).build();
            }
            response.put("success", true);
            response.put("data", driver);
            return Response.ok(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }   
    }

    @Path("user/{user_id}/drivers")
    @GET
    public Response getUserDriversByUserId(@PathParam("user_id") long user_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("user_id", user_id);
        validationString.put("user_id", "exists:users.id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            List<Map<String, Object>> drivers = DB.table("drivers")
            .select("drivers.id as driver_id", "drivers.name as driver_name", 
                    "drivers.email as driver_email", "drivers.phone as driver_phone", 
                    "drivers.avatar as driver_avatar", "user_id", "users.name as user_name", 
                    "users.email as user_email",  "users.phone as user_phone", 
                    "users.avatar as user_avatar", "users.public_id", "drivers.disabled", 
                    "drivers.updated_at", "drivers.created_at")
            .join("users", "users.id", "=", "drivers.user_id")
            .where("partner_id", auth().getUserId())
            .where("user_id", user_id)
            .get();
            if (drivers == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to user with ID: " + user_id});
                return response(OK).entity(response).build();
            }
            response.put("success", true);
            response.put("data", drivers);
            return Response.ok(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }   
    }

    @Path("public/user/{public_id}/drivers")
    @GET
    public Response getUserDriversByUserPublicId(@PathParam("public_id") long public_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("public_id", public_id);
        validationString.put("public_id", "exists:users");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            List<Map<String, Object>> drivers = DB.table("drivers")
            .select("drivers.id as driver_id", "drivers.name as driver_name", 
                    "drivers.email as driver_email", "drivers.phone as driver_phone", 
                    "drivers.avatar as driver_avatar", "user_id", "users.name as user_name", 
                    "users.email as user_email",  "users.phone as user_phone", 
                    "users.avatar as user_avatar", "users.public_id", "drivers.disabled", 
                    "drivers.updated_at", "drivers.created_at")
            .join("users", "users.id", "=", "drivers.user_id")
            .where("partner_id", auth().getUserId())
            .where("public_id", public_id)
            .get();
            if (drivers == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to user with public ID: " + public_id});
                return response(OK).entity(response).build();
            }
            response.put("success", true);
            response.put("data", drivers);
            return Response.ok(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }   
    }

    @Path("drivers")
    @GET
    public Response getDrivers() {
        Map<String, Object> response = new LinkedHashMap<>();
        List<Map<String, Object>> drivers = DB.table("drivers")
        .select("drivers.id as driver_id", "drivers.name as driver_name", 
                "drivers.email as driver_email", "drivers.phone as driver_phone", 
                "drivers.avatar as driver_avatar", "user_id", "users.name as user_name", 
                "users.email as user_email",  "users.phone as user_phone", 
                "users.avatar as user_avatar", "users.public_id", "drivers.disabled", 
                "drivers.updated_at", "drivers.created_at")
        .join("users", "users.id", "=", "drivers.user_id")
        .where("partner_id", auth().getUserId())
        .get();
        if (drivers == null) {
            response.put("success", false);
            response.put("errors", new String[] {"You have no drivers"});
            return response(OK).entity(response).build();
        }
        response.put("success", true);
        response.put("data", drivers);
        return Response.ok(response).build(); 
    }

    @Path("add/driver/to/user/{user_id}")
    @POST
    public Response addUserDriverByUserId(
        @PathParam("user_id") long user_id,
        Map<String, Object> request
    ) {
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
            Map<String, Object> user = DB.table("users").where("partner_id", auth().getUserId())
            .where("id", user_id).first();
            if (user == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to user with ID: " + user_id});
                return response(OK).entity(response).build();
            }
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

    @Path("add/driver/to/public/user/{public_id}")
    @POST
    public Response addUserDriverByUserPublicId(
        @PathParam("public_id") long public_id, 
        Map<String, Object> request
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("public_id", public_id);
        validationValues.put("name", request.get("name"));
        validationValues.put("email", request.get("email"));
        validationValues.put("phone", request.get("phone"));

        validationString.put("public_id", "exists:users");
        validationString.put("name", "required");
        validationString.put("email", "unique:drivers|required");
        validationString.put("phone", "unique:drivers|required");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> user = DB.table("users").where("partner_id", auth().getUserId())
            .where("public_id", public_id).first();
            if (user == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to user with public ID: " + public_id});
                return response(OK).entity(response).build();
            }
            String password;
            if (request.containsKey("password") && request.get("password") != null) {
                password = request.get("password").toString();
            } else {password = request.get("phone").toString();}

            Hashing.HashingResult hashingResult = Hashing.createHash(password);
            request.put("password", hashingResult.getHash());
            request.put("salt", hashingResult.getSalt());
            request.put("user_id", user.get("id"));

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
    public Response updateUserDriverByDriverId(
        @PathParam("driver_id") long driver_id, 
        Map<String, Object> request
    ) {
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
        validationValues.put("public_id", request.get("public_id"));
        validationValues.put("email", request.get("email"));
        validationValues.put("phone", request.get("phone"));

        validationString.put("driver_id", "exists:drivers.id");
        validationString.put("user_id", "exists:users.id");
        validationString.put("public_id", "exists:users");
        validationString.put("email", "unique:drivers");
        validationString.put("phone", "unique:drivers");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> driver = DB.table("drivers")
            .join("users", "users.id", "=", "drivers.user_id")
            .where("partner_id", auth().getUserId())
            .where("drivers.id", driver_id).first();
            if (driver == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to driver with ID: " + driver_id});
                return response(OK).entity(response).build();
            }
            if (request.containsKey("user_id")) {
                Map<String, Object> user = DB.table("users").where("partner_id", auth().getUserId())
                .where("id", request.get("user_id")).first();
                if (user == null) {
                    response.put("success", false);
                    response.put("errors", new String[] {"You have no access to user with ID: " + request.get("user_id")});
                    return response(OK).entity(response).build();
                }
            }
            if (request.containsKey("public_id")) {
                Map<String, Object> user = DB.table("users").where("partner_id", auth().getUserId())
                .where("public_id", request.get("public_id")).first();
                if (user == null) {
                    response.put("success", false);
                    response.put("errors", new String[] {"You have no access to user with public ID: " + request.get("public_id")});
                    return response(OK).entity(response).build();
                }
                request.remove("public_id");
                request.put("user_id", user.get("id"));
            }
            if (request.containsKey("password") && request.get("password") != null) {
                String password = request.get("password").toString();
                Hashing.HashingResult hashingResult = Hashing.createHash(password);
                request.put("password", hashingResult.getHash());
                request.put("salt", hashingResult.getSalt());
            }
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
    public Response destroyDriverByDriverId(@PathParam("driver_id") long driver_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();
        validationValues.put("driver_id", driver_id);
        validationString.put("driver_id", "exists:drivers.id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
             Map<String, Object> driver = DB.table("drivers")
            .join("users", "users.id", "=", "drivers.user_id")
            .where("partner_id", auth().getUserId())
            .where("drivers.id", driver_id).first();
            if (driver == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to driver with ID: " + driver_id});
                return response(OK).entity(response).build();
            }
            response.put("success", DB.table("drivers").where("id", driver_id).delete());
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

//===============================================================================================================================================

    @Path("last/position/by/device/id/{device_id}")
    @GET
    public Response getLastPositionByDeviceId(@PathParam("device_id") String device_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("device_id", device_id);
        validationString.put("device_id", "exists:devices.id|isset:devices.position_id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> position = DB.table("devices")
            .select("position_id","user_id", "device_id", "unique_id", "alarm", 
                    "latitude", "longitude", "altitude", "valid", "motion->val as motion",
                    "address", "speed->val as speed", "acceleration->val as acceleration", 
                    "engine->status as engine_status", "engine->fuel_level as fuel_level",
                    "engine->fuel_drop_rate as fuel_consumption_rate", 
                    "engine->fuel_run_out_after as fuel_run_out_after",
                    "engine->fuel_last_filling_since as fuel_last_filling_since",
                    "course", "accuracy", "positions.attributes", "battery",
                    "device_time", "fix_time", "devices.created_at")
            .join("positions", "devices.position_id", "=", "positions.id")
            .join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", auth().getUserId())
            .where("devices.id", device_id)
            .first();
            
            if (position == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with ID: " + device_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            position.put("battery", Position.getBatteryValue((Map<String, Object>) position.get("battery")));
            response.put("success", true);
            response.put("data", position);
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("last/position/by/device/unique/id/{unique_id}")
    @GET
    public Response getLastPositionByDeviceUniqueId(@PathParam("unique_id") String unique_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("unique_id", unique_id);
        validationString.put("unique_id", "exists:devices|isset:devices.position_id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> position = DB.table("devices")
            .select("position_id","user_id", "device_id", "unique_id", "alarm", 
                    "latitude", "longitude", "altitude", "valid", "motion->val as motion",
                    "address", "speed->val as speed", "acceleration->val as acceleration", 
                    "engine->status as engine_status", "engine->fuel_level as fuel_level",
                    "engine->fuel_drop_rate as fuel_consumption_rate", 
                    "engine->fuel_run_out_after as fuel_run_out_after",
                    "engine->fuel_last_filling_since as fuel_last_filling_since",
                    "course", "accuracy", "positions.attributes", "battery",
                    "device_time", "fix_time", "devices.created_at")
            .join("positions", "devices.position_id", "=", "positions.id")
            .join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", auth().getUserId())
            .where("unique_id", unique_id)
            .first();
            
            if (position == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with Unique ID: " + unique_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            position.put("battery", Position.getBatteryValue((Map<String, Object>) position.get("battery")));
            response.put("success", true);
            response.put("data", position);
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("list/of/positions/by/device/id/{device_id}")
    @GET
    public Response getListOfPositionsByDeviceId(
        @PathParam("device_id") String device_id,
        @QueryParam("from") String from, 
        @QueryParam("to") String to
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("device_id", device_id);
        validationString.put("device_id", "exists:devices.id|isset:devices.position_id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
           Eloquent eloquent = DB.table("devices")
            .select("positions.id as position_id","user_id", "device_id", "unique_id", 
                    "alarm", "latitude", "longitude", "altitude", "valid", "motion", 
                    "address", "speed->val as speed", "acceleration->val as acceleration", 
                    "engine->status as engine_status", "engine->fuel_level as fuel_level",
                    "engine->fuel_drop_rate as fuel_consumption_rate", 
                    "engine->fuel_run_out_after as fuel_run_out_after",
                    "engine->fuel_last_filling_since as fuel_last_filling_since",
                    "course", "accuracy", "positions.attributes", "battery",
                    "device_time", "fix_time", "devices.created_at")
            .join("positions", "devices.id", "=", "positions.device_id")
            .join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", auth().getUserId())
            .where("devices.id", device_id);
            
            if (eloquent.first() == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with ID: " + device_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            List<Map<String, Object>> positions = eloquent.where("positions.created_at", ">=", from)
            .where("positions.created_at", "<=", to).get();
            response.put("success", true);
            response.put("data", positions);
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("list/of/positions/by/device/unique/id/{unique_id}")
    @GET
    public Response getListOfPositionsByDeviceUniqueId(
        @PathParam("unique_id") String unique_id,
        @QueryParam("from") String from, 
        @QueryParam("to") String to
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("unique_id", unique_id);
        validationString.put("unique_id", "exists:devices|isset:devices.position_id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Eloquent eloquent = DB.table("devices")
            .select("positions.id as position_id","user_id", "device_id", "unique_id", 
                    "alarm", "latitude", "longitude", "altitude", "valid", "motion", 
                    "address", "speed->val as speed", "acceleration->val as acceleration", 
                    "engine->status as engine_status", "engine->fuel_level as fuel_level",
                    "engine->fuel_drop_rate as fuel_consumption_rate", 
                    "engine->fuel_run_out_after as fuel_run_out_after",
                    "engine->fuel_last_filling_since as fuel_last_filling_since",
                    "course", "accuracy", "positions.attributes", "battery",
                    "device_time", "fix_time", "devices.created_at")
            .join("positions", "devices.id", "=", "positions.device_id")
            .join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", auth().getUserId())
            .where("unique_id", unique_id);
            
            if (eloquent.first() == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with Unique ID: " + unique_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            List<Map<String, Object>> positions = eloquent.where("positions.created_at", ">=", from)
            .where("positions.created_at", "<=", to).get();
            response.put("success", true);
            response.put("data", positions);
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

//===============================================================================================================================================

    @Path("report/by/device/id/{device_id}")
    @GET
    public Response getDeviceReportByDeviceId(
        @PathParam("device_id") String device_id,
        @QueryParam("from") String from, 
        @QueryParam("to") String to
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("device_id", device_id);
        validationString.put("device_id", "exists:devices.id|isset:devices.position_id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Eloquent eloquent = DB.table("devices")
            .select("speed->min as min_speed", "speed->max as max_speed", "speed->avg as avg_speed",
                    "acceleration->min as min_acceleration", "acceleration->max as max_acceleration",
                    "acceleration->avg as avg_acceleration", "duration->sum as duration", "address",
                    "distance->sum as distance", "engine->work_duration as work_duration", 
                    "engine->fuel_used_sum as total_fuel_consumed", "latitude", "longitude",
                    "engine->fuel_drop_rate_min as min_fuel_consumption_rate", "altitude",
                    "engine->fuel_drop_rate_max as max_fuel_consumption_rate", 
                    "engine->fuel_filling_count as fuel_filling_count")
            .join("positions", "devices.id", "=", "positions.device_id")
            .join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", auth().getUserId())
            .where("devices.id", device_id);
            
            if (eloquent.first() == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with ID: " + device_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            eloquent = eloquent.where("positions.created_at", ">=", from).where("positions.created_at", "<=", to);
            List<Map<String, Object>> positions = eloquent.get();
            if (positions == null) {
                response.put("success", true);
                response.put("data", null);
                return response(OK).entity(response).build();
            }
            Map<String, Object> firstPosition = eloquent.first();
            Map<String, Object> lastPosition = eloquent.last();
            List<Map<String, Object>> locations = new ArrayList<>();
            for (int i = 0; i < positions.size(); i++) {
                Map<String, Object> locationEntry = new LinkedHashMap<>();
                locationEntry.put("latitude", positions.get(i).get("latitude"));
                locationEntry.put("longitude", positions.get(i).get("longitude"));
                locationEntry.put("altitude", positions.get(i).get("altitude"));
                locations.add(locationEntry);
            }
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("distance", ((double) lastPosition.remove("distance") - 
                                   (double) firstPosition.remove("distance")));
            summary.put("duration", ((long) lastPosition.remove("duration") - 
                                   (long) firstPosition.remove("duration")));
            Map<String, Object> route = new LinkedHashMap<>();
            route.put("summary", summary);
            route.put("locations", locations);

            lastPosition.remove("altitude");
            lastPosition.put("route", route);
            lastPosition.put("start_address", firstPosition.remove("address"));
            lastPosition.put("start_latitude", firstPosition.remove("latitude"));
            lastPosition.put("start_longitude", firstPosition.remove("longitude"));
            lastPosition.put("end_address", lastPosition.remove("address"));
            lastPosition.put("end_latitude", lastPosition.remove("latitude"));
            lastPosition.put("end_longitude", lastPosition.remove("longitude"));
            lastPosition.put("engine_work_duration", ((long) lastPosition.remove("work_duration") -
                                   (long) firstPosition.get("work_duration")));
            lastPosition.put("total_fuel_consumed", ((double) lastPosition.get("total_fuel_consumed") - 
                                   (double) firstPosition.get("total_fuel_consumed")));
            lastPosition.put("fuel_filling_count", ((long) lastPosition.get("fuel_filling_count") -
                                   (long) firstPosition.get("fuel_filling_count")));

            response.put("success", true);
            response.put("data", lastPosition);
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("report/by/device/unique/id/{unique_id}")
    @GET
    public Response getDeviceReportByDeviceUniqueId(
        @PathParam("unique_id") String unique_id,
        @QueryParam("from") String from, 
        @QueryParam("to") String to
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("unique_id", unique_id);
        validationString.put("unique_id", "exists:devices|isset:devices.position_id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Eloquent eloquent = DB.table("devices")
            .select("speed->min as min_speed", "speed->max as max_speed", "speed->avg as avg_speed",
                    "acceleration->min as min_acceleration", "acceleration->max as max_acceleration",
                    "acceleration->avg as avg_acceleration", "duration->sum as duration", "address",
                    "distance->sum as distance", "engine->work_duration as work_duration",
                    "engine->fuel_used_sum as total_fuel_consumed", "latitude", "longitude",
                    "engine->fuel_drop_rate_min as min_fuel_consumption_rate", "altitude",
                    "engine->fuel_drop_rate_max as max_fuel_consumption_rate",
                    "engine->fuel_filling_count as fuel_filling_count")
            .join("positions", "devices.id", "=", "positions.device_id")
            .join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", auth().getUserId())
            .where("unique_id", unique_id);
            
            if (eloquent.first() == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with Unique ID: " + unique_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            eloquent = eloquent.where("positions.created_at", ">=", from).where("positions.created_at", "<=", to);
            List<Map<String, Object>> positions = eloquent.get();
            if (positions == null) {
                response.put("success", true);
                response.put("data", null);
                return response(OK).entity(response).build();
            }
            Map<String, Object> firstPosition = eloquent.first();
            Map<String, Object> lastPosition = eloquent.last();
            List<Map<String, Object>> locations = new ArrayList<>();
            for (int i = 0; i < positions.size(); i++) {
                Map<String, Object> locationEntry = new LinkedHashMap<>();
                locationEntry.put("latitude", positions.get(i).get("latitude"));
                locationEntry.put("longitude", positions.get(i).get("longitude"));
                locationEntry.put("altitude", positions.get(i).get("altitude"));
                locations.add(locationEntry);
            }
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("distance", ((double) lastPosition.remove("distance") - 
                                   (double) firstPosition.remove("distance")));
            summary.put("duration", ((long) lastPosition.remove("duration") - 
                                   (long) firstPosition.remove("duration")));
            Map<String, Object> route = new LinkedHashMap<>();
            route.put("summary", summary);
            route.put("locations", locations);

            lastPosition.remove("altitude");
            lastPosition.put("route", route);
            lastPosition.put("start_address", firstPosition.remove("address"));
            lastPosition.put("start_latitude", firstPosition.remove("latitude"));
            lastPosition.put("start_longitude", firstPosition.remove("longitude"));
            lastPosition.put("end_address", lastPosition.remove("address"));
            lastPosition.put("end_latitude", lastPosition.remove("latitude"));
            lastPosition.put("end_longitude", lastPosition.remove("longitude"));
            lastPosition.put("engine_work_duration", ((long) lastPosition.remove("work_duration") -
                                   (long) firstPosition.get("work_duration")));
            lastPosition.put("total_fuel_consumed", ((double) lastPosition.get("total_fuel_consumed") - 
                                   (double) firstPosition.get("total_fuel_consumed")));
            lastPosition.put("fuel_filling_count", ((long) lastPosition.get("fuel_filling_count") -
                                   (long) firstPosition.get("fuel_filling_count")));

            response.put("success", true);
            response.put("data", lastPosition);
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

//===============================================================================================================================================

    @Path("history/by/device/id/{device_id}")
    @GET
    public Response getDeviceHistoryByDeviceId(
        @PathParam("device_id") String device_id,
        @QueryParam("from") String from, 
        @QueryParam("to") String to
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("device_id", device_id);
        validationString.put("device_id", "exists:devices.id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Eloquent eloquent = DB.table("devices").join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", auth().getUserId()).where("devices.id", device_id);
            if (eloquent.first() == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with ID: " + device_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            eloquent = DB.table("histories")
            .select("id", "device_id", "unique_id","type", "name", "info", "description", "created_at as at")
            .where("device_id", device_id).where("histories.created_at", ">=", from).where("histories.created_at", "<=", to);
            response.put("success", true);
            response.put("data", eloquent.get());
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("history/by/device/unique/id/{unique_id}")
    @GET
    public Response getDeviceHistoryByDeviceUniqueId(
        @PathParam("unique_id") String unique_id,
        @QueryParam("from") String from, 
        @QueryParam("to") String to
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("unique_id", unique_id);
        validationString.put("unique_id", "exists:devices");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Eloquent eloquent = DB.table("devices").join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", auth().getUserId()).where("devices.unique_id", unique_id);
            if (eloquent.first() == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with Unique ID: " + unique_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            eloquent = DB.table("histories")
            .select("id", "device_id", "unique_id","type", "name", "info", "description", "created_at as at")
            .where("unique_id", unique_id).where("histories.created_at", ">=", from).where("histories.created_at", "<=", to);
            response.put("success", true);
            response.put("data", eloquent.get());
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

//===============================================================================================================================================

    @Path("maintenance/by/device/id/{device_id}")
    @GET
    public Response getDeviceMaintenancesByDeviceId(@PathParam("device_id") String device_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("device_id", device_id);
        validationString.put("device_id", "exists:devices.id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Eloquent eloquent = DB.table("devices").join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", auth().getUserId()).where("devices.id", device_id);
            if (eloquent.first() == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with ID: " + device_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            eloquent = DB.table("maintenances")
            .select("id", "device_id", "unique_id","type", "name", "attribute_value", "maintain", 
                    "maintained", "maintain_count", "periodically", "updated_at", "created_at")
            .where("device_id", device_id);
            response.put("success", true);
            response.put("data", eloquent.get());
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("maintenance/by/device/unique/id/{unique_id}")
    @GET
    public Response getDeviceMaintenancesByDeviceUniqueId(@PathParam("unique_id") String unique_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("unique_id", unique_id);
        validationString.put("unique_id", "exists:devices");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Eloquent eloquent = DB.table("devices").join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", auth().getUserId()).where("unique_id", unique_id);
            if (eloquent.first() == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with unique ID: " + unique_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            eloquent = DB.table("maintenances")
            .select("id", "device_id", "unique_id","type", "name", "attribute_value", "maintain", 
                    "maintained", "maintain_count", "periodically", "updated_at", "created_at")
            .where("unique_id", unique_id);
            response.put("success", true);
            response.put("data", eloquent.get());
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("maintenance/{maintenance_id}")
    @GET
    public Response getDeviceMaintenanceByMaintenanceId(@PathParam("maintenance_id") String maintenance_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("maintenance_id", maintenance_id);
        validationString.put("maintenance_id", "exists:maintenances.id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Eloquent eloquent = DB.table("devices").join("users", "devices.user_id", "=", "users.id")
            .join("maintenances", "devices.id", "=", "maintenances.device_id")
            .where("partner_id", auth().getUserId()).where("maintenances.id", maintenance_id);
            if (eloquent.first() == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to maintenance ID: " + maintenance_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            eloquent = DB.table("maintenances")
            .select("id", "device_id", "unique_id","type", "name", "attribute_value", "maintain", 
                    "maintained", "maintain_count", "periodically", "updated_at", "created_at");
            response.put("success", true);
            response.put("data", eloquent.find(maintenance_id));
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("maintenance/by/device/id/{device_id}")
    @POST
    public Response createDeviceMaintenancesByDeviceId(
        @PathParam("device_id") String device_id,
        Map<String, Object> request
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("device_id", device_id);
        validationValues.put("type", request.get("type"));
        validationValues.put("attribute_value", request.get("attribute_value"));
        validationString.put("device_id", "exists:devices.id");
        validationString.put("type", "required");
        validationString.put("attribute_value", "required");

        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> device = DB.table("devices").join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", auth().getUserId()).where("devices.id", device_id).first();
            if (device == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with ID: " + device_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            if (!Maintenance.checkMaintenance(request.get("type").toString())) {
                response.put("success", false);
                response.put("errors", new String[] {"Unknown maintenance type: " + request.get("type")});
                return response(BAD_REQUEST).entity(response).build();
            }
            request.put("device_id", device_id);
            request.put("unique_id", device.get("unique_id"));
            response.put("success", true);
            response.put("data", DB.table("maintenances").create(request));
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("maintenance/by/device/unique/id/{unique_id}")
    @POST
    public Response createDeviceMaintenancesByDeviceUniqueId(
        @PathParam("unique_id") String unique_id,
        Map<String, Object> request
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("unique_id", unique_id);
        validationValues.put("type", request.get("type"));
        validationValues.put("attribute_value", request.get("attribute_value"));
        validationString.put("unique_id", "exists:devices");
        validationString.put("type", "required");
        validationString.put("attribute_value", "required");

        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Map<String, Object> device = DB.table("devices").join("users", "devices.user_id", "=", "users.id")
            .where("partner_id", auth().getUserId()).where("unique_id", unique_id).first();
            if (device == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to device with unique ID: " + unique_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            if (!Maintenance.checkMaintenance(request.get("type").toString())) {
                response.put("success", false);
                response.put("errors", new String[] {"Unknown maintenance type: " + request.get("type")});
                return response(BAD_REQUEST).entity(response).build();
            }
            request.put("unique_id", unique_id);
            request.put("device_id", device.get("id"));
            response.put("success", true);
            response.put("data", DB.table("maintenances").create(request));
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("maintenance/{maintenance_id}")
    @PUT
    public Response deleteDeviceMaintenanceByMaintenanceId(
        @PathParam("maintenance_id") String maintenance_id,
        Map<String, Object> request
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request == null) {
            response.put("success", false);
            response.put("errors", new String[] {"Please set input data."});
            return response(BAD_REQUEST).entity(response).build();
        }
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("maintenance_id", maintenance_id);
        validationValues.put("device_id", request.get("device_id"));
        validationValues.put("unique_id", request.get("unique_id"));

        validationString.put("maintenance_id", "exists:maintenances.id");
        validationString.put("device_id", "exists:devices.id");
        validationString.put("unique_id", "exists:devices");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Eloquent eloquent = DB.table("devices").join("users", "devices.user_id", "=", "users.id")
            .join("maintenances", "devices.id", "=", "maintenances.device_id")
            .where("partner_id", auth().getUserId()).where("maintenances.id", maintenance_id);
            if (eloquent.first() == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to maintenance ID: " + maintenance_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            if (request.containsKey("device_id") && request.containsKey("unique_id")) {
                if (!DB.table("devices").find(request.get("device_id")).get("unique_id")
                       .toString().equals(request.get("unique_id").toString())) {
                    response.put("success", false);
                    response.put("errors", new String[] {"The unique id of the device with ID: " + 
                    request.get("device_id") + " not equals: " + request.get("unique_id")});
                    return response(BAD_REQUEST).entity(response).build();
                }
            }
            if (request.containsKey("device_id") && !request.containsKey("unique_id")) {
                request.put("unique_id", DB.table("devices").find(request.get("device_id")).get("unique_id"));
            }
            if (!request.containsKey("device_id") && request.containsKey("unique_id")) {
                request.put("device_id", 
                DB.table("devices").where("unique_id", request.get("unique_id")).first().get("device_id"));
            }
            response.put("success", true);
            response.put("data", DB.table("maintenances").where("id", maintenance_id).update(request));
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }

    @Path("maintenance/{maintenance_id}")
    @DELETE
    public Response deleteDeviceMaintenanceByMaintenanceId(@PathParam("maintenance_id") String maintenance_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> validationValues = new LinkedHashMap<>();
        Map<String, String> validationString = new LinkedHashMap<>();

        validationValues.put("maintenance_id", maintenance_id);
        validationString.put("maintenance_id", "exists:maintenances.id");
        
        Validator validator = validate(validationValues, validationString);
        if (validator.validated()) {
            Eloquent eloquent = DB.table("devices").join("users", "devices.user_id", "=", "users.id")
            .join("maintenances", "devices.id", "=", "maintenances.device_id")
            .where("partner_id", auth().getUserId()).where("maintenances.id", maintenance_id);
            if (eloquent.first() == null) {
                response.put("success", false);
                response.put("errors", new String[] {"You have no access to maintenance ID: " + maintenance_id});
                return response(BAD_REQUEST).entity(response).build();
            }
            response.put("success", DB.table("maintenances").where("id", maintenance_id).delete());
            return response(OK).entity(response).build();
        } else {
            response.put("success", false);
            response.put("errors", validator.getErrors());
            return response(BAD_REQUEST).entity(response).build();
        }
    }
    
    @Path("maintenance/types")
    @GET
    public Response getMaintenanceTypes() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", Maintenance.getTypes());
        return response(OK).entity(response).build();
    }
}
