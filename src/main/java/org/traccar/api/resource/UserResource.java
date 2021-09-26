/*
 * Copyright 2015 - 2017 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.api.resource;

import org.traccar.validator.Validator;
import org.traccar.Context;
import org.traccar.api.BaseObjectResource;
import org.traccar.database.UsersManager;
import org.traccar.helper.LogAction;
import org.traccar.model.User;
import org.traccar.helper.ServletHelper;

import javax.servlet.http.HttpServletRequest;
import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

import java.util.*;

@Path("partner")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource extends BaseObjectResource<User> {

    @javax.ws.rs.core.Context
    private HttpServletRequest request;

    public UserResource() {
        super(User.class);
    }

    @GET
    public Collection<User> get(@QueryParam("userId") long userId) throws SQLException {
        UsersManager usersManager = Context.getUsersManager();
        Set<Long> result;
        if (Context.getPermissionsManager().getUserAdmin(getUserId())) {
            if (userId != 0) {
                result = usersManager.getUserItems(userId);
            } else {
                result = usersManager.getAllItems();
            }
        } else if (Context.getPermissionsManager().getUserManager(getUserId())) {
            result = usersManager.getManagedItems(getUserId());
        } else {
            throw new SecurityException("Unauthorized Access");
        }
        return usersManager.getItems(result);
    }

    @Path("register")
    @PermitAll
    @POST
    public Response register(User entity) throws SQLException {
    
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("email", entity.getEmail());
        request.put("phone", entity.getPhone());
        
        Map<String, String> validationString = new LinkedHashMap<>();
        validationString.put("email", "unique:user|required");
        validationString.put("phone", "unique:user|required");
        
        Map<String, Object> response = new LinkedHashMap<>();
        Validator validator = Validator.validate(request, validationString);
        if (validator.validated()) {
        
            Context.getUsersManager().addItem(entity);
            LogAction.create(getUserId(), entity);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("userId", entity.getId());
            response.put("success", true);
            response.put("data", data);
            return Response.ok(response).build();
            
        } else {
            response.put("success", false);
            response.put("error", validator.getErrors());
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }
    }

    @Path("id")
    @PermitAll
    @POST
    public Response login(User entity) throws SQLException {
    
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("email", entity.getEmail());
        
        Map<String, String> validationString = new LinkedHashMap<>();
        validationString.put("email", "exists:user|required");
    
        Map<String, Object> response = new LinkedHashMap<>();
        Validator validator = Validator.validate(request, validationString);
        if (validator.validated()) {
        
            String email = null;
            if(entity.getName() != null && entity.getName().equals("admin")) {
                email = entity.getName();
            } else {
                email = entity.getEmail();
            }
            Map<String, Object> data = new LinkedHashMap<>();
            String password = entity.getPasswordToAdmin();
            User user = Context.getPermissionsManager().login(email, password);
            if (user != null) {
                LogAction.login(user.getId());
                data.put("userId", user.getId());
                response.put("success", true);
                response.put("data", data);
                return Response.ok(response).build();
            } else {
                data.put("message", "Invalid Password");
                response.put("success", false);
                response.put("error", data);
                return Response.status(Response.Status.NOT_FOUND).entity(response).build();
            }
            
        } else {
            response.put("success", false);
            response.put("error", validator.getErrors());
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }
    }

}
