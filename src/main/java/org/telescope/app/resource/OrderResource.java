
package org.telescope.app.resource;

import org.telescope.app.SimpleObjectResource;
import org.telescope.model.Order;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource extends SimpleObjectResource<Order> {

    public OrderResource() {
        super(Order.class);
    }

}
