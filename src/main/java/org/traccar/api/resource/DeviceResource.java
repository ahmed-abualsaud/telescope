/*
 * Copyright 2015 - 2018 Anton Tananaev (anton@traccar.org)
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
import org.traccar.database.DeviceManager;
import org.traccar.helper.LogAction;
import org.traccar.model.Device;
import org.traccar.model.User;
import org.traccar.model.DeviceAccumulators;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;

import java.sql.SQLException;
import java.util.*;

@Path("devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceResource extends BaseObjectResource<Device> {

    public DeviceResource() {
        super(Device.class);
    }
    
    @Path("all")
    @GET
    public Response list() {
        DeviceManager deviceManager = Context.getDeviceManager();
        Set<Long> result = deviceManager.getAllItems();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", deviceManager.getItems(result));
        return Response.ok(response).build();
    }

    @Path("partner/{partnerId}")
    @GET
    public Response get(@PathParam("partnerId") long partnerId, 
    		@QueryParam("uniqueId") List<String> uniqueIds) throws SQLException {
    		
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("partnerId", partnerId);
        request.put("uniqueId", uniqueIds);
        
        Map<String, String> validationString = new LinkedHashMap<>();
        validationString.put("partnerId", "exists:user.id");
        validationString.put("uniqueId", "exists:device|multiple");
        
        Map<String, Object> response = new LinkedHashMap<>();
        Validator validator = Validator.validate(request, validationString);
        if (validator.validated()) {
        
            DeviceManager deviceManager = Context.getDeviceManager();
            Set<Long> result;
            if (uniqueIds.isEmpty()) {
                result = deviceManager.getUserItems(partnerId);
            } else {
                result = new HashSet<>();
                for (String uniqueId : uniqueIds) {
                    Device device = deviceManager.getByUniqueId(uniqueId);
                    Context.getPermissionsManager().checkDevice(partnerId, device.getId());
                    result.add(device.getId());
                }
            }
            response.put("success", true);
            response.put("data", deviceManager.getItems(result));
            return Response.ok(response).build();
            
        } else {
            response.put("success", false);
            response.put("error", validator.getErrors());
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }
    }

    @Path("partner/{partnerId}")
    @POST
    public Response add(Device entity, @PathParam("partnerId") long partnerId) throws SQLException {
    
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("partnerId", partnerId);
        request.put("name", entity.getName());
        request.put("uniqueId", entity.getUniqueId());
        
        Map<String, String> validationString = new LinkedHashMap<>();
        validationString.put("partnerId", "exists:user.id");
        validationString.put("name", "unique:device|required");
        validationString.put("uniqueId", "unique:device|required");
        
        Map<String, Object> response = new LinkedHashMap<>();
        Validator validator = Validator.validate(request, validationString);
        if (validator.validated()) {
        
            Context.getPermissionsManager().checkDeviceLimit(partnerId);
            Context.getDeviceManager().addItem(entity);
            LogAction.create(partnerId, entity);
            Context.getDataManager().linkObject(User.class, partnerId, Device.class, entity.getId(), true);
            LogAction.link(partnerId, User.class, partnerId, Device.class, entity.getId());
            Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
            Context.getPermissionsManager().refreshAllExtendedPermissions();
            response.put("success", true);
            response.put("data", entity);
            return Response.ok(response).build();
            
        } else {
            response.put("success", false);
            response.put("error", validator.getErrors());
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }
    }
}
