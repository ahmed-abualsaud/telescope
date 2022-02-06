package org.traccar.api.resource;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import org.traccar.Context;
import org.traccar.config.Keys;
import org.traccar.api.auth.AuthResource;

import java.util.Map;
import java.util.LinkedHashMap;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.common.hash.Hashing;

@Path("app")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PusherResource extends AuthResource {

    @Path("ABCDEFGHIJKLM/{val1}/{val2}/{val3}")
    @POST
    public Response pusher() {
        return response(OK).entity(null).build();
    }
}
