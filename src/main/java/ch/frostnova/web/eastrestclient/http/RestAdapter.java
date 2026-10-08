package ch.frostnova.web.eastrestclient.http;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.frostnova.web.eastrestclient.http.MediaTypes.APPLICATION_JSON;
import static ch.frostnova.web.eastrestclient.http.MediaTypes.APPLICATION_XML;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class RestAdapter {

    private static final AtomicInteger requestSequence = new AtomicInteger();

    private static final Logger logger = LoggerFactory.getLogger(RestAdapter.class);

    private static final String CONTENT_TYPE = "content-type";

    private final HttpClient httpClient;
    private final ObjectMapper json;
    private final ObjectMapper xml;

    public RestAdapter(HttpClient httpClient, ObjectMapper json, ObjectMapper xml) {
        this.httpClient = requireNonNull(httpClient);
        this.json = requireNonNull(json);
        this.xml = requireNonNull(xml);
    }

    ObjectMapper getJson() {
        return json;
    }

    ObjectMapper getXml() {
        return xml;
    }

    public Object invoke(RequestMethod method, URI uri, Map<String, List<String>> headers,
                         RequestBody body, Type returnType, ResponseSink sink) throws IOException, InterruptedException {

        var sequenceId = requestSequence.incrementAndGet();
        logger.debug("{} > {} {}", sequenceId, method, uri);

        var sendsBody = method == RequestMethod.POST || method == RequestMethod.PUT;

        var requestBuilder = HttpRequest.newBuilder().uri(uri);
        headers.forEach((key, values) -> values.forEach(value -> {
            requestBuilder.header(key, value);
            logger.debug("{} > {}: {}", sequenceId, key, value);
        }));
        if (sendsBody && body.isPresent() && body.getContentType() != null) {
            requestBuilder.header(CONTENT_TYPE, body.getContentType());
            logger.debug("{} > {}: {}", sequenceId, CONTENT_TYPE, body.getContentType());
        }
        if (sendsBody && body.getDescription() != null) {
            logger.debug("{} > {}", sequenceId, body.getDescription());
        }

        var publisher = body.isPresent() ? body.getPublisher() : HttpRequest.BodyPublishers.noBody();
        switch (method) {
            case GET:
                requestBuilder.GET();
                break;
            case POST:
                requestBuilder.POST(publisher);
                break;
            case PUT:
                requestBuilder.PUT(publisher);
                break;
            case DELETE:
                requestBuilder.DELETE();
                break;
            default:
                throw new UnsupportedOperationException("unsupported request method: " + method);
        }

        var request = requestBuilder.build();
        var startNanos = System.nanoTime();
        if (sink.isPresent()) {
            return executeStreaming(request, sink, sequenceId, startNanos);
        }
        return executeBuffered(request, returnType, sequenceId, startNanos);
    }

    private Object executeBuffered(HttpRequest request, Type returnType, int sequenceId, long startNanos) throws IOException, InterruptedException {

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        var body = response.body();
        var contentType = response.headers().firstValue(CONTENT_TYPE).orElse(null);
        var charset = MediaTypes.charset(contentType, UTF_8);
        logResponse(sequenceId, response, body, charset);
        logRequestSummary(request, response.statusCode(), startNanos);

        var message = body != null && body.length > 0 ? new String(body, charset) : null;
        HttpErrorHandler.checkResponse(response, message);

        if (body == null || body.length == 0) {
            return null;
        }
        if (Void.class.equals(returnType) || void.class.equals(returnType)) {
            return null;
        }
        if (String.class.equals(returnType)) {
            return new String(body, charset);
        }
        if (MediaTypes.matches(contentType, APPLICATION_JSON)) {
            var javaType = json.getTypeFactory().constructType(returnType);
            return json.readValue(body, javaType);
        }
        if (MediaTypes.matches(contentType, APPLICATION_XML)) {
            var javaType = xml.getTypeFactory().constructType(returnType);
            return xml.readValue(body, javaType);
        }
        if (MediaTypes.matches(contentType, "text/*")) {
            return new String(body, charset);
        }
        throw new UnsupportedOperationException("unsupported response media type: " + contentType + " for " + returnType);
    }

    private Object executeStreaming(HttpRequest request, ResponseSink sink, int sequenceId, long startNanos) throws IOException, InterruptedException {

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        var contentType = response.headers().firstValue(CONTENT_TYPE).orElse(null);
        var charset = MediaTypes.charset(contentType, UTF_8);
        logResponseHeaders(sequenceId, response);

        try (var in = response.body()) {
            try {
                if (response.statusCode() / 100 == 4 || response.statusCode() / 100 == 5) {
                    var errorBody = in.readAllBytes();
                    var message = errorBody.length > 0 ? new String(errorBody, charset) : null;
                    HttpErrorHandler.checkResponse(response, message);
                }
                copy(in, sink);
            } finally {
                logRequestSummary(request, response.statusCode(), startNanos);
            }
        }
        logger.debug("{} < <{}>", sequenceId, sink.getKind().name().toLowerCase());
        switch (sink.getKind()) {
            case PATH:
                return sink.getPath();
            case FILE:
                return sink.getFile();
            default:
                return null;
        }
    }

    private void copy(InputStream in, ResponseSink sink) throws IOException {
        switch (sink.getKind()) {
            case OUTPUT_STREAM:
                in.transferTo(sink.getOutputStream());
                break;
            case PATH:
                try (var out = Files.newOutputStream(sink.getPath())) {
                    in.transferTo(out);
                }
                break;
            case FILE:
                try (var out = Files.newOutputStream(sink.getFile().toPath())) {
                    in.transferTo(out);
                }
                break;
            default:
                throw new UnsupportedOperationException("unsupported response sink: " + sink.getKind());
        }
    }

    private void logRequestSummary(HttpRequest request, int statusCode, long startNanos) {
        var elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000.0;
        var status = Response.Status.fromStatusCode(statusCode);
        var statusText = status != null ? statusCode + " " + status.getReasonPhrase() : String.valueOf(statusCode);
        logger.info("{} {} -> {}, {} ms", request.method(), request.uri(), statusText,
                String.format(Locale.ROOT, "%.2f", elapsedMillis));
    }

    private void logResponse(int sequenceId, HttpResponse<?> response, byte[] body, Charset charset) {
        logResponseHeaders(sequenceId, response);
        if (body != null && body.length > 0) {
            logger.debug("{} < {}", sequenceId, new String(body, charset));
        }
    }

    private void logResponseHeaders(int sequenceId, HttpResponse<?> response) {
        logger.debug("{} < {} {}", sequenceId, response.statusCode(), Response.Status.fromStatusCode(response.statusCode()));
        response.headers().map().forEach((key, values) -> logger.debug("{} < {}: {}", sequenceId, key, String.join(";", values)));
    }
}
