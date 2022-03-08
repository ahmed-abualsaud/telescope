
package org.telescope.app.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.telescope.app.SimpleObjectResource;
import org.telescope.model.Calendar;

@Path("calendars")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CalendarResource extends SimpleObjectResource<Calendar> {

    public CalendarResource() {
        super(Calendar.class);
    }

}
