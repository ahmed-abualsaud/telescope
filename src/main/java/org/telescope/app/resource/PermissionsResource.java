
package org.telescope.app.resource;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.telescope.app.BaseResource;
import org.telescope.javel.framework.helper.LogAction;
import org.telescope.model.Device;
import org.telescope.model.Permission;
import org.telescope.model.User;
import org.telescope.server.Context;

@Path("permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PermissionsResource  extends BaseResource {

    private void checkPermission(Permission permission, boolean link) {
        if (!link && permission.getOwnerClass().equals(User.class)
                && permission.getPropertyClass().equals(Device.class)) {
            if (getUserId() != permission.getOwnerId()) {
                Context.getPermissionsManager().checkUser(getUserId(), permission.getOwnerId());
            } else {
                Context.getPermissionsManager().checkAdmin(getUserId());
            }
        } else {
            Context.getPermissionsManager().checkPermission(
                    permission.getOwnerClass(), getUserId(), permission.getOwnerId());
        }
        Context.getPermissionsManager().checkPermission(
                permission.getPropertyClass(), getUserId(), permission.getPropertyId());
    }

    private void checkPermissionTypes(List<LinkedHashMap<String, Long>> entities) {
        Set<String> keys = null;
        for (LinkedHashMap<String, Long> entity: entities) {
            if (keys != null & !entity.keySet().equals(keys)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());
            }
            keys = entity.keySet();
        }
    }

    @Path("bulk")
    @POST
    public Response add(List<LinkedHashMap<String, Long>> entities) throws SQLException, ClassNotFoundException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        checkPermissionTypes(entities);
        for (LinkedHashMap<String, Long> entity: entities) {
            Permission permission = new Permission(entity);
            checkPermission(permission, true);
            Context.getDataManager().linkObject(permission.getOwnerClass(), permission.getOwnerId(),
                    permission.getPropertyClass(), permission.getPropertyId(), true);
            LogAction.link(getUserId(), permission.getOwnerClass(), permission.getOwnerId(),
                    permission.getPropertyClass(), permission.getPropertyId());
        }
        if (!entities.isEmpty()) {
            Context.getPermissionsManager().refreshPermissions(new Permission(entities.get(0)));
        }
        return Response.noContent().build();
    }

    @POST
    public Response add(LinkedHashMap<String, Long> entity) throws SQLException, ClassNotFoundException {
        return add(Collections.singletonList(entity));
    }

    @DELETE
    @Path("bulk")
    public Response remove(List<LinkedHashMap<String, Long>> entities) throws SQLException, ClassNotFoundException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        checkPermissionTypes(entities);
        for (LinkedHashMap<String, Long> entity: entities) {
            Permission permission = new Permission(entity);
            checkPermission(permission, false);
            Context.getDataManager().linkObject(permission.getOwnerClass(), permission.getOwnerId(),
                    permission.getPropertyClass(), permission.getPropertyId(), false);
            LogAction.unlink(getUserId(), permission.getOwnerClass(), permission.getOwnerId(),
                    permission.getPropertyClass(), permission.getPropertyId());
        }
        if (!entities.isEmpty()) {
            Context.getPermissionsManager().refreshPermissions(new Permission(entities.get(0)));
        }
        return Response.noContent().build();
    }

    @DELETE
    public Response remove(LinkedHashMap<String, Long> entity) throws SQLException, ClassNotFoundException {
        return remove(Collections.singletonList(entity));
    }

}
