
package org.telescope.app;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.telescope.javel.framework.helper.Log;

public class ResourceErrorHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        if (e instanceof WebApplicationException) {
            WebApplicationException webException = (WebApplicationException) e;
            return Response.fromResponse(webException.getResponse()).entity(Log.exceptionStack(webException)).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(Log.exceptionStack(e)).build();
        }
    }

}
