
package org.telescope.app.resource;

import org.telescope.app.BaseResource;
import org.telescope.model.Statistics;
import org.telescope.server.Context;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

@Path("statistics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StatisticsResource extends BaseResource {

    @GET
    public Collection<Statistics> get(
            @QueryParam("from") Date from, @QueryParam("to") Date to) throws SQLException {
        Context.getPermissionsManager().checkAdmin(getUserId());
        return Context.getDataManager().getStatistics(from, to);
    }

}
