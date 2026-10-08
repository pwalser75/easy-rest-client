package ch.frostnova.web.eastrestclient.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A multipart/form-data request body, composed of one or more parts.
 * <p>
 * The body is streamed (no part content is buffered in memory beyond the part that is currently being transferred),
 * using an {@link HttpRequest.BodyPublisher} that concatenates the part headers and part content streams. As the
 * total content length is not known upfront, the request will be sent with chunked transfer encoding.
 */
public final class MultipartBody {

    private final String boundary;
    private final Charset charset;
    private final List<Part> parts = new ArrayList<>();

    public MultipartBody() {
        this(UTF_8);
    }

    public MultipartBody(Charset charset) {
        this.charset = charset;
        this.boundary = "----EasyRestClientBoundary" + UUID.randomUUID().toString().replace("-", "");
    }

    public String getBoundary() {
        return boundary;
    }

    /**
     * Returns the content type header value to use for this multipart body, including the boundary parameter.
     *
     * @return multipart content type
     */
    public String getContentType() {
        return MediaTypes.MULTIPART_FORM_DATA + "; boundary=" + boundary;
    }

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    public MultipartBody addText(String name, String value, String mediaType) {
        var content = value != null ? value : "";
        var type = mediaType != null ? mediaType : MediaTypes.TEXT_PLAIN + "; charset=" + charset.name();
        return addPart(name, null, type, () -> new ByteArrayInputStream(content.getBytes(charset)));
    }

    public MultipartBody addBytes(String name, String filename, String mediaType, byte[] content) {
        var bytes = content != null ? content : new byte[0];
        return addPart(name, filename, mediaType, () -> new ByteArrayInputStream(bytes));
    }

    public MultipartBody addFile(String name, String filename, String mediaType, Path file) {
        var resolvedFilename = filename != null ? filename : file.getFileName().toString();
        var type = mediaType != null ? mediaType : MediaTypes.APPLICATION_OCTET_STREAM;
        return addPart(name, resolvedFilename, type, () -> {
            try {
                return Files.newInputStream(file);
            } catch (Exception ex) {
                throw new MultipartException("cannot read file part: " + file, ex);
            }
        });
    }

    public MultipartBody addStream(String name, String filename, String mediaType, Supplier<InputStream> content) {
        var type = mediaType != null ? mediaType : MediaTypes.APPLICATION_OCTET_STREAM;
        return addPart(name, filename, type, content);
    }

    private MultipartBody addPart(String name, String filename, String mediaType, Supplier<InputStream> content) {
        parts.add(new Part(name, filename, mediaType, content));
        return this;
    }

    /**
     * Creates a body publisher that streams the multipart content.
     *
     * @return body publisher for this multipart body
     */
    public HttpRequest.BodyPublisher toBodyPublisher() {
        return HttpRequest.BodyPublishers.ofInputStream(this::openStream);
    }

    InputStream openStream() {
        var streams = new ArrayList<InputStream>();
        for (Part part : parts) {
            streams.add(new ByteArrayInputStream(partHeader(part)));
            streams.add(part.content.get());
            streams.add(new ByteArrayInputStream(("\r\n").getBytes(charset)));
        }
        streams.add(new ByteArrayInputStream(("--" + boundary + "--\r\n").getBytes(charset)));
        return new SequenceInputStream(Collections.enumeration(streams));
    }

    private byte[] partHeader(Part part) {
        var header = new StringBuilder();
        header.append("--").append(boundary).append("\r\n");
        header.append("Content-Disposition: form-data; name=\"").append(escape(part.name)).append("\"");
        if (part.filename != null) {
            header.append("; filename=\"").append(escape(part.filename)).append("\"");
        }
        header.append("\r\n");
        if (part.mediaType != null) {
            header.append("Content-Type: ").append(part.mediaType).append("\r\n");
        }
        header.append("\r\n");
        return header.toString().getBytes(charset);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "");
    }

    private static final class Part {

        private final String name;
        private final String filename;
        private final String mediaType;
        private final Supplier<InputStream> content;

        private Part(String name, String filename, String mediaType, Supplier<InputStream> content) {
            this.name = name;
            this.filename = filename;
            this.mediaType = mediaType;
            this.content = content;
        }
    }

    /**
     * Exception thrown when a multipart part cannot be read.
     */
    public static class MultipartException extends RuntimeException {

        public MultipartException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
