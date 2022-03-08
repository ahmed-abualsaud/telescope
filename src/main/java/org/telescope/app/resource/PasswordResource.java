
package org.telescope.app.resource;

import org.apache.velocity.VelocityContext;
import org.telescope.app.BaseResource;
import org.telescope.javel.framework.notification.FullMessage;
import org.telescope.javel.framework.notification.TextTemplateFormatter;
import org.telescope.model.User;
import org.telescope.server.Context;

import javax.annotation.security.PermitAll;
import javax.mail.MessagingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.UUID;

@Path("password")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class PasswordResource extends BaseResource {

    private static final String PASSWORD_RESET_TOKEN = "passwordToken";

    @Path("reset")
    @PermitAll
    @POST
    public Response reset(@FormParam("email") String email) throws SQLException, MessagingException {
        for (long userId : Context.getUsersManager().getAllItems()) {
            User user = Context.getUsersManager().getById(userId);
            if (email.equals(user.getEmail())) {
                String token = UUID.randomUUID().toString().replaceAll("-", "");
                user.set(PASSWORD_RESET_TOKEN, token);
                Context.getUsersManager().updateItem(user);
                VelocityContext velocityContext = TextTemplateFormatter.prepareContext(null);
                velocityContext.put("token", token);
                FullMessage message = TextTemplateFormatter.formatFullMessage(velocityContext, "passwordReset");
                Context.getMailManager().sendMessage(userId, message.getSubject(), message.getBody());
                break;
            }
        }
        return Response.ok().build();
    }

    @Path("update")
    @PermitAll
    @POST
    public Response update(
            @FormParam("token") String token, @FormParam("password") String password) throws SQLException {
        for (long userId : Context.getUsersManager().getAllItems()) {
            User user = Context.getUsersManager().getById(userId);
            if (token.equals(user.getString(PASSWORD_RESET_TOKEN))) {
                user.getAttributes().remove(PASSWORD_RESET_TOKEN);
                user.setPassword(password);
                Context.getUsersManager().updateItem(user);
                return Response.ok().build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

}
