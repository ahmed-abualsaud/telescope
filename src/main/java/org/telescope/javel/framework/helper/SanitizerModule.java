
package org.telescope.javel.framework.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.owasp.encoder.Encode;

import java.io.IOException;

public class SanitizerModule extends SimpleModule {

    public static class SanitizerSerializer extends StdSerializer<String> {

        protected SanitizerSerializer() {
            super(String.class);
        }

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(Encode.forHtml(value));
        }

    }

    public SanitizerModule() {
        addSerializer(new SanitizerSerializer());
    }

}
