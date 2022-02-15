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

@Path("partner")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PartnerResource extends AuthResource {

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
        .where("partner_id", auth().getUserId()).where("id", public_id).first();
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
            .where("id", public_id).update(request);
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
        response.put("success", DB.table("users").where("partner_id", auth()
        .getUserId()).where("id", user_id).delete());
        return response(OK).entity(response).build();
    }

    @Path("user/public/id/{public_id}")
    @DELETE
    public Response destroyUserByPublicId(@PathParam("public_id") long public_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", DB.table("users").where("partner_id", auth()
        .getUserId()).where("id", public_id).delete());
        return response(OK).entity(response).build();
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

//================================================================================================================================================

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
}
