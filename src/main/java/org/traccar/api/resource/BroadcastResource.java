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

@Path("broadcasting")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class BroadcastResource extends AuthResource {

    @Path("auth")
    @POST
    public Response authenticate(
        @FormParam("socket_id") String socket_id,
        @FormParam("channel_name") String channel_name
    ) {
        String PUSHER_KEY = Context.getConfig().getString(Keys.PUSHER_APP_KEY);
        String PUSHER_SECRET = Context.getConfig().getString(Keys.PUSHER_APP_SECRET);
        String DATA = socket_id + ":" + channel_name;
        Charset charset= StandardCharsets.UTF_8;
        String hash = Hashing.hmacSha256(PUSHER_SECRET.getBytes(charset)).hashString(DATA, charset).toString();
        hash = PUSHER_KEY + ":" + hash;
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("auth", hash);
        return response(OK).entity(Context.jsonEncode(response)).build();
    }
}
