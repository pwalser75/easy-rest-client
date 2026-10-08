package ch.frostnova.web.eastrestclient.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static ch.frostnova.web.eastrestclient.http.MediaTypes.APPLICATION_JSON;
import static ch.frostnova.web.eastrestclient.http.MediaTypes.APPLICATION_OCTET_STREAM;
import static ch.frostnova.web.eastrestclient.http.MediaTypes.APPLICATION_XML;
import static ch.frostnova.web.eastrestclient.http.MediaTypes.TEXT_PLAIN;
import static ch.frostnova.web.eastrestclient.http.MediaTypes.matches;

/**
 * Creates {@link RequestBody} instances from bound method arguments, supporting structured content (JSON, XML, text),
 * URL-encoded forms, multipart forms, and binary content (files, streams, byte arrays, body publishers and
 * streaming writers).
 */
final class RequestBodyFactory {

    private RequestBodyFactory() {

    }

    @SuppressWarnings("unchecked")
    static RequestBody encode(Object body, String consumes, ObjectMapper json, ObjectMapper xml, Charset charset)
            throws IOException {
        if (body == null) {
            return RequestBody.none();
        }
        if (body instanceof HttpRequest.BodyPublisher) {
            return RequestBody.of((HttpRequest.BodyPublisher) body, contentOr(consumes, APPLICATION_OCTET_STREAM), "<stream>");
        }
        if (body instanceof byte[]) {
            var bytes = (byte[]) body;
            return RequestBody.of(HttpRequest.BodyPublishers.ofByteArray(bytes), contentOr(consumes, APPLICATION_OCTET_STREAM), "<binary " + bytes.length + " bytes>");
        }
        if (body instanceof ByteBuffer) {
            var buffer = ((ByteBuffer) body).duplicate();
            var bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return RequestBody.of(HttpRequest.BodyPublishers.ofByteArray(bytes), contentOr(consumes, APPLICATION_OCTET_STREAM), "<binary buffer>");
        }
        if (body instanceof Path) {
            var path = (Path) body;
            return RequestBody.of(HttpRequest.BodyPublishers.ofFile(path), contentOr(consumes, APPLICATION_OCTET_STREAM), "<file " + path.getFileName() + ">");
        }
        if (body instanceof File) {
            var file = (File) body;
            return RequestBody.of(HttpRequest.BodyPublishers.ofFile(file.toPath()), contentOr(consumes, APPLICATION_OCTET_STREAM), "<file " + file.getName() + ">");
        }
        if (body instanceof InputStream) {
            var stream = (InputStream) body;
            return RequestBody.of(HttpRequest.BodyPublishers.ofInputStream(() -> stream), contentOr(consumes, APPLICATION_OCTET_STREAM), "<input stream>");
        }
        if (body instanceof Consumer) {
            var writer = (Consumer<OutputStream>) body;
            return RequestBody.of(writeToPublisher(writer), contentOr(consumes, APPLICATION_OCTET_STREAM), "<streamed>");
        }
        if (matches(consumes, APPLICATION_XML)) {
            var bytes = xml.writeValueAsBytes(body);
            return RequestBody.of(HttpRequest.BodyPublishers.ofByteArray(bytes), contentOr(consumes, APPLICATION_XML), new String(bytes, charset));
        }
        if (matches(consumes, TEXT_PLAIN) || body instanceof CharSequence) {
            var text = String.valueOf(body);
            return RequestBody.of(HttpRequest.BodyPublishers.ofString(text, charset), contentOr(consumes, TEXT_PLAIN), text);
        }
        var bytes = json.writeValueAsBytes(body);
        return RequestBody.of(HttpRequest.BodyPublishers.ofByteArray(bytes), contentOr(consumes, APPLICATION_JSON), new String(bytes, charset));
    }

    static RequestBody encodeForm(List<FormField> fields, Charset charset) {
        var formFields = new LinkedHashMap<String, List<String>>();
        for (FormField field : fields) {
            for (Object value : valuesOf(field.getValue())) {
                formFields.computeIfAbsent(field.getName(), key -> new ArrayList<>())
                        .add(value != null ? String.valueOf(value) : "");
            }
        }
        var content = FormEncoder.encode(formFields, charset);
        return RequestBody.of(HttpRequest.BodyPublishers.ofString(content, charset), MediaTypes.APPLICATION_FORM_URLENCODED, content);
    }

    static RequestBody encodeMultipart(List<FormField> fields, ObjectMapper json, Charset charset)
            throws JsonProcessingException {
        var multipart = new MultipartBody(charset);
        var partCount = 0;
        for (FormField field : fields) {
            for (Object value : valuesOf(field.getValue())) {
                addMultipartPart(multipart, field.getName(), value, json);
                partCount++;
            }
        }
        return RequestBody.of(multipart.toBodyPublisher(), multipart.getContentType(),
                MediaTypes.MULTIPART_FORM_DATA + " (" + partCount + " part(s))");
    }

    private static List<Object> valuesOf(Object value) {
        if (value instanceof Path) {
            return java.util.Collections.singletonList(value);
        }
        if (value instanceof Iterable) {
            List<Object> values = new ArrayList<>();
            ((Iterable<?>) value).forEach(values::add);
            return values;
        }
        if (value != null && value.getClass().isArray()) {
            var length = java.lang.reflect.Array.getLength(value);
            List<Object> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                values.add(java.lang.reflect.Array.get(value, i));
            }
            return values;
        }
        return java.util.Collections.singletonList(value);
    }

    private static void addMultipartPart(MultipartBody multipart, String name, Object value, ObjectMapper json)
            throws JsonProcessingException {
        if (value instanceof Path) {
            var path = (Path) value;
            multipart.addFile(name, null, probeMediaType(path), path);
        } else if (value instanceof File) {
            var file = (File) value;
            multipart.addFile(name, file.getName(), probeMediaType(file.toPath()), file.toPath());
        } else if (value instanceof byte[]) {
            multipart.addBytes(name, null, APPLICATION_OCTET_STREAM, (byte[]) value);
        } else if (value instanceof InputStream) {
            var stream = (InputStream) value;
            multipart.addStream(name, null, APPLICATION_OCTET_STREAM, () -> stream);
        } else if (value instanceof ByteBuffer) {
            var buffer = ((ByteBuffer) value).duplicate();
            var bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            multipart.addBytes(name, null, APPLICATION_OCTET_STREAM, bytes);
        } else if (isStructured(value)) {
            multipart.addBytes(name, null, APPLICATION_JSON, json.writeValueAsBytes(value));
        } else {
            multipart.addText(name, value != null ? String.valueOf(value) : "", null);
        }
    }

    private static boolean isStructured(Object value) {
        return value != null
                && !(value instanceof CharSequence || value instanceof Number || value instanceof Boolean
                || value instanceof Character || value instanceof Enum);
    }

    private static String probeMediaType(Path path) {
        try {
            var probed = Files.probeContentType(path);
            return probed != null ? probed : APPLICATION_OCTET_STREAM;
        } catch (IOException ex) {
            return APPLICATION_OCTET_STREAM;
        }
    }

    private static String contentOr(String consumes, String fallback) {
        return consumes != null ? consumes : fallback;
    }

    /**
     * Creates a body publisher that is fed by the given writer callback, using a background thread and a pipe, so the
     * content does not have to be materialized in memory.
     */
    static HttpRequest.BodyPublisher writeToPublisher(Consumer<OutputStream> writer) {
        return HttpRequest.BodyPublishers.ofInputStream(() -> {
            var input = new PipedInputStream(8192);
            var output = new PipedOutputStream();
            try {
                output.connect(input);
            } catch (IOException ex) {
                throw new UncheckedIOException("cannot connect stream pipe", ex);
            }
            var thread = new Thread(() -> {
                try (OutputStream stream = output) {
                    writer.accept(stream);
                } catch (IOException ex) {
                    throw new UncheckedIOException("failed writing request body", ex);
                }
            }, "rest-client-body-writer");
            thread.setDaemon(true);
            thread.start();
            return input;
        });
    }
}
