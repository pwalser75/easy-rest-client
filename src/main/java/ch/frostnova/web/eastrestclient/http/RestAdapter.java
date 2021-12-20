package ch.frostnova.web.eastrestclient.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;

public class RestAdapter {

    private final HttpClient httpClient;
    private final ObjectMapper json;

    public RestAdapter(HttpClient httpClient, ObjectMapper json) {
        this.httpClient = requireNonNull(httpClient);
        this.json = requireNonNull(json);
    }

    public <B, T> T invoke(RequestMethod method, URI uri,
                           Consumes consumes, Produces produces,
                           B body, Class<T> returnType) throws IOException, InterruptedException {

        System.out.println(method + " " + uri + " -> " + returnType.getSimpleName());

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(uri);
        if (method == RequestMethod.GET) {
            requestBuilder.GET();
        }
        if (method == RequestMethod.POST) {
            requestBuilder.header("content-type", consumes.value()[0])
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        }
        if (method == RequestMethod.PUT) {
            requestBuilder.header("content-type", consumes.value()[0])
                    .PUT(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        }
        if (method == RequestMethod.DELETE) {
            requestBuilder.DELETE();
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponseStatus(response);
        String plain = response.body();

        if (String.class.isAssignableFrom(returnType)) {
            return (T) plain;
        }
        if (returnType == Void.class) {
            return null;
        }
        if (produces == null) {
            return null;
        }
        if (Arrays.stream(produces.value()).anyMatch(mediaType -> mediaType.equals(MediaType.APPLICATION_JSON))) {
            if (plain == null || plain.length() == 0) {
                return null;
            }
            return json.readValue(plain, returnType);
        }
        throw new UnsupportedOperationException("unknown or unsupported media type: " + produces + ", " + returnType);
    }

    private void checkResponseStatus(HttpResponse<?> response) {
        int status = response.statusCode();
        if (status == 404) {
            throw new NoSuchElementException(status + " NOT FOUND");
        }
        if (status / 100 == 4) {
            throw new RuntimeException(status + " CLIENT ERROR");
        }
        if (status / 100 == 5) {
            throw new RuntimeException(status + " SERVER ERROR");
        }
        if (status < 100 || status > 599) {
            throw new RuntimeException(status + " UNKNOWN ERROR");
        }
    }
}
