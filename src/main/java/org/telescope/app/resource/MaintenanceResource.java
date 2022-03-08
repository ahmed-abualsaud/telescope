
package org.telescope.app.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.telescope.app.ExtendedObjectResource;
import org.telescope.model.Maintenance;

@Path("maintenance")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaintenanceResource extends ExtendedObjectResource<Maintenance> {

    public MaintenanceResource() {
        super(Maintenance.class);
    }

}
