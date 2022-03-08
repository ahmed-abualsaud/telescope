
package org.telescope.app;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import org.telescope.javel.framework.helper.DateUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;

public class DateParameterConverterProvider implements ParamConverterProvider {

    public static class DateParameterConverter implements ParamConverter<Date> {

        @Override
        public Date fromString(String value) {
            return value != null ? DateUtil.parseDate(value) : null;
        }

        @Override
        public String toString(Date value) {
            return value != null ? DateUtil.formatDate(value) : null;
        }

    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (Date.class.equals(rawType)) {
            @SuppressWarnings("unchecked")
            ParamConverter<T> paramConverter = (ParamConverter<T>) new DateParameterConverter();
            return paramConverter;
        }
        return null;
    }

}
