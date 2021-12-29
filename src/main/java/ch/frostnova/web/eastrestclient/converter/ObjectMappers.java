package ch.frostnova.web.eastrestclient.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;

/**
 * Object mappers. Currently, only supports JSON, but other formats such as YAML and XML could be added here as well.
 */
public final class ObjectMappers {

    private ObjectMappers() {

    }

    public static ObjectMapper json() {
        return configure(new ObjectMapper());
    }

    public static ObjectMapper xml() {
        JacksonXmlModule xmlModule = new JacksonXmlModule();
        xmlModule.setDefaultUseWrapper(false);
        return configure(new XmlMapper(xmlModule));
    }

    private static ObjectMapper configure(ObjectMapper mapper) {
        return mapper
                .setAnnotationIntrospector(new JacksonAnnotationIntrospector())
                .registerModule(new JavaTimeModule())
                .setDateFormat(new StdDateFormat())
                .enable(ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .enable(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .disable(WRITE_DATES_AS_TIMESTAMPS)
                .disable(WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .disable(FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(NON_EMPTY);
    }
}
