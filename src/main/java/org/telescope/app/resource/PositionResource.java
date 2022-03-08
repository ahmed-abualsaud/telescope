package org.telescope.app.resource;

import org.telescope.app.BaseResource;
import org.telescope.database.DeviceManager;
import org.telescope.javel.framework.validator.Validator;
import org.telescope.model.Position;
import org.telescope.server.Context;
import org.telescope.model.Device;

import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.*;

@Path("positions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PositionResource extends BaseResource {

    @Path("partner/{partnerId}")
    @GET
    public void getJson(@PathParam("partnerId") long partnerId,
            @QueryParam("uniqueId") String uniqueId,
            @QueryParam("from") Date from, @QueryParam("to") Date to)
            throws SQLException {
            
        /*Map<String, Object> request = new LinkedHashMap<>();
        request.put("partnerId", partnerId);
        request.put("uniqueId", uniqueId);
        
        Map<String, String> validationString = new LinkedHashMap<>();
        validationString.put("partnerId", "exists:user.id");
        validationString.put("uniqueId", "exists:device|required");
        
        Map<String, Object> response = new LinkedHashMap<>();
        Validator validator = Validator.validate(request, validationString);
        if (validator.validated()) {
        
            Collection<Position> positions;
            DeviceManager deviceManager = Context.getDeviceManager();
            Device device = deviceManager.getByUniqueId(uniqueId);
            Context.getPermissionsManager().checkDevice(partnerId, device.getId());
            if (from != null && to != null) {
                positions = Context.getDataManager().getPositions(device.getId(), from, to);
            } else {
                positions = Collections.singleton(Context.getDeviceManager().getLastPosition(device.getId()));
            }
            response.put("success", true);
            response.put("data", positions);
            return Response.ok(response).build();
            
        } else {
            response.put("success", false);
            response.put("error", validator.getErrors());
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }*/
    }

}
