package ch.frostnova.web.eastrestclient.http;

import java.net.http.HttpRequest;

import static java.util.Objects.requireNonNull;

/**
 * An encoded HTTP request body: the body publisher, the content type to declare, and a human readable description
 * used for logging (so that binary content is not written to the log).
 */
final class RequestBody {

    private static final RequestBody NONE = new RequestBody(HttpRequest.BodyPublishers.noBody(), null, null, false);

    private final HttpRequest.BodyPublisher publisher;
    private final String contentType;
    private final String description;
    private final boolean present;

    private RequestBody(HttpRequest.BodyPublisher publisher, String contentType, String description, boolean present) {
        this.publisher = publisher;
        this.contentType = contentType;
        this.description = description;
        this.present = present;
    }

    static RequestBody none() {
        return NONE;
    }

    static RequestBody of(HttpRequest.BodyPublisher publisher, String contentType, String description) {
        return new RequestBody(requireNonNull(publisher), contentType, description, true);
    }

    HttpRequest.BodyPublisher getPublisher() {
        return publisher;
    }

    String getContentType() {
        return contentType;
    }

    String getDescription() {
        return description;
    }

    boolean isPresent() {
        return present;
    }
}
