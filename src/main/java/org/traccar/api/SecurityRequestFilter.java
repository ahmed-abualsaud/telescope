/*
 * Copyright 2015 - 2016 Anton Tananaev (anton@traccar.org)
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
package org.traccar.api;

import org.traccar.Context;
import org.traccar.Main;
import org.traccar.database.StatisticsManager;
import org.traccar.helper.DataConverter;
import org.traccar.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import java.util.*;

public class SecurityRequestFilter implements ContainerRequestFilter {

    public static final String API_KEY = "api_key";

    public static String[] decodeBasicAuth(String auth) {
        auth = auth.replaceFirst("[B|b]asic ", "");
        byte[] decodedBytes = DataConverter.parseBase64(auth);
        if (decodedBytes != null && decodedBytes.length > 0) {
            return new String(decodedBytes, StandardCharsets.US_ASCII).split(":", 2);
        }
        return null;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {

        if (requestContext.getMethod().equals("OPTIONS")) {
            return;
        }

        SecurityContext securityContext = null;
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        //String apiKey = requestContext.getUriInfo().getQueryParameters().getFirst(API_KEY);
        String authHeader = requestContext.getHeaderString("Authorization");
        
        if (authHeader != null) {
            String[] auth = decodeBasicAuth(authHeader);
            if(auth[0].equals("qruz") && auth[1].equals("123456789")) {
                try {
                    User user = Context.getPermissionsManager().login("admin", "admin");
                    if (user != null) {
                        Main.getInjector().getInstance(StatisticsManager.class).registerRequest(user.getId());
                        securityContext = new UserSecurityContext(new UserPrincipal(user.getId()));
                        requestContext.setSecurityContext(securityContext);
                        return;
                    } else {
                        data.put("message", "Provided user credentials not found");
                    }
                } catch (SQLException e) {
                    data.put("message", e.getMessage());
                    response.put("success", false);
                    response.put("error", data);
                    requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build());
                    return;
                }
            } else {
                data.put("message", "Invalid authentication token");
            }
        } else {
            data.put("message", "Please set the Authentication header");
        }
        response.put("success", false);
        response.put("error", data);
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(response).build());
        return;
    }

}
