
package org.telescope.app;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.telescope.server.Context;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return Context.getObjectMapper();
    }

}
