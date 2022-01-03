package ch.frostnova.web.eastrestclient.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class RestAdapter {

    private final static AtomicInteger requestSequence = new AtomicInteger();

    private final static Logger logger = LoggerFactory.getLogger(RestAdapter.class);

    private final HttpClient httpClient;
    private final ObjectMapper json;
    private final ObjectMapper xml;

    public RestAdapter(HttpClient httpClient, ObjectMapper json, ObjectMapper xml) {
        this.httpClient = requireNonNull(httpClient);
        this.json = requireNonNull(json);
        this.xml = requireNonNull(xml);
    }

    private <B> String serializeBody(B body, String contentType) throws JsonProcessingException {
        if (body == null) {
            return null;
        }
        if (APPLICATION_JSON.equals(contentType)) {
            return json.writeValueAsString(body);
        }
        if (APPLICATION_XML.equals(contentType)) {
            return xml.writeValueAsString(body);
        }
        return String.valueOf(body);
    }

    public <B, T> T invoke(RequestMethod method, URI uri, Map<String, String> headers,
                           Consumes consumes, Produces produces,
                           B body, Type returnType) throws IOException, InterruptedException {

        int sequenceId = requestSequence.incrementAndGet();

        logger.info("{} > {} {}", sequenceId, method, uri);

        String consumesContentType = Optional.ofNullable(consumes).map(Consumes::value).map(Arrays::stream).flatMap(Stream::findFirst).orElse(null);
        String serializedBody = serializeBody(body, consumesContentType);

        if (serializedBody != null) {
            logger.info("{} > {}", sequenceId, serializedBody);
        }
        if (consumesContentType != null) {
            headers.put("content-type", consumesContentType);
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(uri);
        headers.forEach(requestBuilder::header);
        headers.forEach((key, value) -> logger.info("{} > {}: {}", sequenceId, key, value));

        if (method == RequestMethod.GET) {
            requestBuilder.GET();
        }
        if (method == RequestMethod.POST) {
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(serializedBody));
        }
        if (method == RequestMethod.PUT) {
            requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(serializedBody));
        }
        if (method == RequestMethod.DELETE) {
            requestBuilder.DELETE();
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        logger.info("{} < {} {}", sequenceId, response.statusCode(), Response.Status.fromStatusCode(response.statusCode()));
        HttpHeaders responseHeaders = response.headers();
        if (responseHeaders != null) {
            responseHeaders.map().forEach((key, values) -> {
                logger.info("{} < {}: {}", sequenceId, key, String.join(";", values));
            });
        }

        String plain = response.body();
        if (plain != null && plain.length() > 0) {
            logger.info("{} < {}", sequenceId, plain);
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
        String contentType = Optional.ofNullable(responseHeaders)
                .flatMap(h -> h.firstValue("content-type"))
                .orElseThrow(() -> new UnsupportedOperationException("undisclosed content-type"));

        if (APPLICATION_JSON.equals(contentType)) {
            JavaType javaType = json.getTypeFactory().constructType(returnType);
            return json.readValue(plain, javaType);
        }
        if (APPLICATION_XML.equals(contentType)) {
            JavaType javaType = xml.getTypeFactory().constructType(returnType);
            return xml.readValue(plain, javaType);
        }
        throw new UnsupportedOperationException("unknown or unsupported media type: " + produces + ", " + returnType);
    }
}
