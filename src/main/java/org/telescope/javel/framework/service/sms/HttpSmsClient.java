
package org.telescope.javel.framework.service.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.config.Keys;
import org.telescope.javel.framework.helper.DataConverter;
import org.telescope.javel.framework.notification.MessageException;
import org.telescope.server.Context;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpSmsClient implements SmsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpSmsClient.class);

    private final String url;
    private final String authorizationHeader;
    private final String authorization;
    private final String template;
    private final boolean encode;
    private final MediaType mediaType;

    public HttpSmsClient() {
        url = Context.getConfig().getString(Keys.SMS_HTTP_URL);
        authorizationHeader = Context.getConfig().getString(Keys.SMS_HTTP_AUTHORIZATION_HEADER);
        if (Context.getConfig().hasKey(Keys.SMS_HTTP_AUTHORIZATION)) {
            authorization = Context.getConfig().getString(Keys.SMS_HTTP_AUTHORIZATION);
        } else {
            String user = Context.getConfig().getString(Keys.SMS_HTTP_USER);
            String password = Context.getConfig().getString(Keys.SMS_HTTP_PASSWORD);
            if (user != null && password != null) {
                authorization = "Basic "
                        + DataConverter.printBase64((user + ":" + password).getBytes(StandardCharsets.UTF_8));
            } else {
                authorization = null;
            }
        }
        template = Context.getConfig().getString(Keys.SMS_HTTP_TEMPLATE).trim();
        if (template.charAt(0) == '{' || template.charAt(0) == '[') {
            encode = false;
            mediaType = MediaType.APPLICATION_JSON_TYPE;
        } else {
            encode = true;
            mediaType = MediaType.APPLICATION_FORM_URLENCODED_TYPE;
        }
    }

    private String prepareValue(String value) throws UnsupportedEncodingException {
        return encode ? URLEncoder.encode(value, StandardCharsets.UTF_8.name()) : value;
    }

    private String preparePayload(String destAddress, String message) {
        try {
            return template
                    .replace("{phone}", prepareValue(destAddress))
                    .replace("{message}", prepareValue(message.trim()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private Invocation.Builder getRequestBuilder() {
        Invocation.Builder builder = Context.getClient().target(url).request();
        if (authorization != null) {
            builder = builder.header(authorizationHeader, authorization);
        }
        return builder;
    }

    @Override
    public void sendMessageSync(String destAddress, String message, boolean command) throws MessageException {
        Response response = getRequestBuilder().post(Entity.entity(preparePayload(destAddress, message), mediaType));
        if (response.getStatus() / 100 != 2) {
            throw new MessageException(response.readEntity(String.class));
        }
    }

    @Override
    public void sendMessageAsync(final String destAddress, final String message, final boolean command) {
        getRequestBuilder().async().post(
                Entity.entity(preparePayload(destAddress, message), mediaType), new InvocationCallback<String>() {
            @Override
            public void completed(String s) {
            }

            @Override
            public void failed(Throwable throwable) {
                LOGGER.warn("SMS send failed", throwable);
            }
        });
    }

}
