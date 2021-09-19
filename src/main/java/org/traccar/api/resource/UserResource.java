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

import org.traccar.Context;
import org.traccar.api.BaseObjectResource;
import org.traccar.database.UsersManager;
import org.traccar.helper.LogAction;
import org.traccar.model.User;
import org.traccar.helper.ServletHelper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
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

@Path("users")
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
            throw new SecurityException("Admin or manager access required");
        }
        return usersManager.getItems(result);
    }

    @Path("register")
    @PermitAll
    @POST
    public Response register(User entity) throws SQLException {
    
        Context.getUsersManager().addItem(entity);
        LogAction.create(getUserId(), entity);
        
        User user = Context.getPermissionsManager().login(entity.getEmail(), entity.getPasswordToAdmin());
        
        if (user != null) {
            LogAction.login(user.getId());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("userId", user.getId());

            return Response.ok(response).build();
            
        } else {
            LogAction.failedLogin(ServletHelper.retrieveRemoteAddress(request));
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    @Path("login")
    @PermitAll
    @POST
    public Response login(User entity) throws SQLException {
    	
    	String email = null;
    
        if(entity.getName() != null && entity.getName().equals("admin")) {
             email = entity.getName();
        } else {
             email = entity.getEmail();
        }

        String password = entity.getPasswordToAdmin();
        User user = Context.getPermissionsManager().login(email, password);

        if (user != null) {
            LogAction.login(user.getId());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("userId", user.getId());

            return Response.ok(response).build();
            
        } else {
            LogAction.failedLogin(ServletHelper.retrieveRemoteAddress(request));
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }


    }

}
