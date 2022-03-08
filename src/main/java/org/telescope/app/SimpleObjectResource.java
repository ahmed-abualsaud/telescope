
package org.telescope.app;

import java.sql.SQLException;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;

import org.telescope.database.BaseObjectManager;
import org.telescope.model.BaseModel;
import org.telescope.server.Context;

public class SimpleObjectResource<T extends BaseModel> extends BaseObjectResource<T> {

    public SimpleObjectResource(Class<T> baseClass) {
        super(baseClass);
    }

    @GET
    public Collection<T> get(
            @QueryParam("all") boolean all, @QueryParam("userId") long userId) throws SQLException {

        BaseObjectManager<T> manager = Context.getManager(getBaseClass());
        return manager.getItems(getSimpleManagerItems(manager, all, userId));
    }

}
