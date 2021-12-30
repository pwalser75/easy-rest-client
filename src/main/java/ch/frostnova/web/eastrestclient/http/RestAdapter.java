package ch.frostnova.web.eastrestclient.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class RestAdapter {

    private final static Logger logger = LoggerFactory.getLogger(RestAdapter.class);

    private final HttpClient httpClient;
    private final ObjectMapper json;
    private final ObjectMapper xml;

    public RestAdapter(HttpClient httpClient, ObjectMapper json, ObjectMapper xml) {
        this.httpClient = requireNonNull(httpClient);
        this.json = requireNonNull(json);
        this.xml = requireNonNull(xml);
    }

    private <B> String serializeBody(B body, Consumes consumes) throws JsonProcessingException {
        if (body == null) {
            return null;
        }
        if (Arrays.stream(consumes.value()).anyMatch(mediaType -> mediaType.equals(APPLICATION_JSON))) {
            return json.writeValueAsString(body);
        }
        if (Arrays.stream(consumes.value()).anyMatch(mediaType -> mediaType.equals(APPLICATION_XML))) {
            return xml.writeValueAsString(body);
        }
        return String.valueOf(body);
    }

    public <B, T> T invoke(RequestMethod method, URI uri, Map<String, String> headers,
                           Consumes consumes, Produces produces,
                           B body, Type returnType) throws IOException, InterruptedException {

        logger.info(">> {} {}", method, uri);

        String serializedBody = serializeBody(body, consumes);

        if (serializedBody != null) {
            logger.info("{}", serializedBody);
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(uri);
        if (method == RequestMethod.GET) {
            requestBuilder.GET();
        }
        if (method == RequestMethod.POST) {
            requestBuilder.header("content-type", consumes.value()[0])
                    .POST(HttpRequest.BodyPublishers.ofString(serializedBody));
        }
        if (method == RequestMethod.PUT) {
            requestBuilder.header("content-type", consumes.value()[0])
                    .PUT(HttpRequest.BodyPublishers.ofString(serializedBody));
        }
        if (method == RequestMethod.DELETE) {
            requestBuilder.DELETE();
        }
        headers.forEach(requestBuilder::header);

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        logger.info("<< {}", response.statusCode());
        String plain = response.body();
        if (plain != null && plain.length() > 0) {
            logger.info("{}", plain);
        }
        HttpErrorHandler.checkResponse(response);
        if (plain == null || plain.length() == 0) {
            return null;
        }

        if (Void.class.equals(returnType)) {
            return null;
        }
        if (String.class.equals(returnType)) {
            return (T) plain;
        }
        if (produces == null) {
            return null;
        }
        if (Arrays.stream(produces.value()).anyMatch(mediaType -> mediaType.equals(APPLICATION_JSON))) {
            JavaType javaType = json.getTypeFactory().constructType(returnType);
            return json.readValue(plain, javaType);
        }
        if (Arrays.stream(produces.value()).anyMatch(mediaType -> mediaType.equals(APPLICATION_XML))) {
            JavaType javaType = xml.getTypeFactory().constructType(returnType);
            return xml.readValue(plain, javaType);
        }
        throw new UnsupportedOperationException("unknown or unsupported media type: " + produces + ", " + returnType);
    }
}
