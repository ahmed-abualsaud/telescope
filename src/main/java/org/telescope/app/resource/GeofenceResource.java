
package org.telescope.app.resource;

import org.telescope.app.ExtendedObjectResource;
import org.telescope.model.Geofence;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("geofences")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GeofenceResource extends ExtendedObjectResource<Geofence> {

    public GeofenceResource() {
        super(Geofence.class);
    }

}
