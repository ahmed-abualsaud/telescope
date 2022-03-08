
package org.telescope.app.resource;

import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.telescope.model.Attribute;
import org.telescope.model.Position;
import org.telescope.server.Context;
import org.telescope.app.ExtendedObjectResource;
import org.telescope.handler.ComputedAttributesHandler;

@Path("attributes/computed")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AttributeResource extends ExtendedObjectResource<Attribute> {

    public AttributeResource() {
        super(Attribute.class);
    }

    @POST
    @Path("test")
    public Response test(@QueryParam("deviceId") long deviceId, Attribute entity) {
        Context.getPermissionsManager().checkAdmin(getUserId());
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        Position last = Context.getIdentityManager().getLastPosition(deviceId);
        if (last != null) {
            Object result = new ComputedAttributesHandler(
                    Context.getConfig(),
                    Context.getIdentityManager(),
                    Context.getAttributesManager()).computeAttribute(entity, last);
            if (result != null) {
                switch (entity.getType()) {
                    case "number":
                        Number numberValue = (Number) result;
                        return Response.ok(numberValue).build();
                    case "boolean":
                        Boolean booleanValue = (Boolean) result;
                        return Response.ok(booleanValue).build();
                    default:
                        return Response.ok(result.toString()).build();
                }
            } else {
                return Response.noContent().build();
            }
        } else {
            throw new IllegalArgumentException("Device has no last position");
        }
    }

    @POST
    public Response add(Attribute entity) throws SQLException {
        Context.getPermissionsManager().checkAdmin(getUserId());
        return super.add(entity);
    }

    @Path("{id}")
    @PUT
    public Response update(Attribute entity) throws SQLException {
        Context.getPermissionsManager().checkAdmin(getUserId());
        return super.update(entity);
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws SQLException {
        Context.getPermissionsManager().checkAdmin(getUserId());
        return super.remove(id);
    }

}
