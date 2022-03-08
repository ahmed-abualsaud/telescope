package org.telescope.javel.framework.resource;

import java.util.Map;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.telescope.javel.framework.auth.UserPrincipal;
import org.telescope.javel.framework.validator.Validator;

import javax.ws.rs.core.Response;

public class Resource {

    @javax.ws.rs.core.Context
    private SecurityContext securityContext;
    
    protected final String OK = "ok";
    protected final String UNAUTHORIZED = "unauthorized";
    protected final String BAD_REQUEST = "bad_request";
    protected final String INTERNAL_SERVER_ERROR = "server_error";
    
    protected UserPrincipal auth() {
        return (UserPrincipal) securityContext.getUserPrincipal();
    }
    
    protected ResponseBuilder response(String status) {

        if (status.equals("ok")) {
            return Response.status(Response.Status.OK);
        }
        if (status.equals("unauthorized")) {
            return Response.status(Response.Status.UNAUTHORIZED);
        }
        if (status.equals("bad_request")) {
            return Response.status(Response.Status.BAD_REQUEST);
        }
        if (status.equals("server_error")) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return null;
    }
    
    protected Validator validate(Map<String, Object> validationValues, Map<String, String> validationString) {
        return Validator.validate(validationValues, validationString);
    }
}
