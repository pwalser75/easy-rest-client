package ch.frostnova.web.eastrestclient.http;

import ch.frostnova.web.eastrestclient.util.StringUtil;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static ch.frostnova.web.eastrestclient.util.StringUtil.pathEncode;
import static ch.frostnova.web.eastrestclient.util.StringUtil.urlEncode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

public class RestMethodAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RestMethodAdapter.class);

    private final Method method;
    private final Type returnType;
    private final Class<?> rawReturnType;
    private final RequestMethod requestMethod;
    private final RestMethodArgument[] arguments;

    private final String classPath;
    private final String methodPath;
    private final String consumes;
    private final String produces;

    public RestMethodAdapter(Method method) {
        this.method = method;
        returnType = method.getGenericReturnType();
        rawReturnType = method.getReturnType();

        requestMethod = determineRequestMethod(method);

        classPath = Optional.ofNullable(method.getDeclaringClass().getAnnotation(Path.class)).map(Path::value).orElse(null);
        methodPath = Optional.ofNullable(method.getAnnotation(Path.class)).map(Path::value).orElse(null);
        consumes = firstMediaType(firstNonNull(method.getAnnotation(Consumes.class),
                method.getDeclaringClass().getAnnotation(Consumes.class)));
        produces = firstMediaType(firstNonNull(method.getAnnotation(Produces.class),
                method.getDeclaringClass().getAnnotation(Produces.class)));

        var parameters = method.getParameters();
        var parameterAnnotations = method.getParameterAnnotations();

        arguments = new RestMethodArgument[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            arguments[i] = toArgument(i, parameters[i], parameterAnnotations[i]);
        }
        validateArguments();

        logger.debug("bound @{} {}.{}({}) -> {}", requestMethod,
                method.getDeclaringClass().getSimpleName(), method.getName(),
                Arrays.stream(arguments).map(String::valueOf).collect(joining(", ")), method.getGenericReturnType());
    }

    private RequestMethod determineRequestMethod(Method method) {
        var getRequest = method.getAnnotation(GET.class);
        var postRequest = method.getAnnotation(POST.class);
        var putRequest = method.getAnnotation(PUT.class);
        var deleteRequest = method.getAnnotation(DELETE.class);

        var matchingAnnotations = Stream.of(getRequest, postRequest, putRequest, deleteRequest).filter(Objects::nonNull).count();
        if (matchingAnnotations > 1) {
            throw new UnsupportedOperationException(String.format("multiple request method annotations found on method %s", method));
        }
        if (matchingAnnotations < 1) {
            throw new UnsupportedOperationException(String.format("no request method annotation found on method %s", method));
        }
        if (getRequest != null) {
            return RequestMethod.GET;
        }
        if (postRequest != null) {
            return RequestMethod.POST;
        }
        if (putRequest != null) {
            return RequestMethod.PUT;
        }
        if (deleteRequest != null) {
            return RequestMethod.DELETE;
        }
        throw new UnsupportedOperationException(String.format("unsupported request method on method %s, only GET,POST,PUT,DELETE are supported", method));
    }

    private RestMethodArgument toArgument(int index, Parameter parameter, Annotation[] annotations) {
        var headerParam = getAnnotation(HeaderParam.class, annotations);
        var pathParam = getAnnotation(PathParam.class, annotations);
        var queryParam = getAnnotation(QueryParam.class, annotations);
        var formParam = getAnnotation(FormParam.class, annotations);

        var matchingAnnotations = Stream.of(headerParam, pathParam, queryParam, formParam).filter(Optional::isPresent).count();
        if (matchingAnnotations > 1) {
            throw new UnsupportedOperationException("more than one param annotation on argument " + index + " on method " + method);
        }
        if (headerParam.isPresent()) {
            return new RestMethodArgument(RestMethodArgumentType.HEADER_PARAM, headerParam.get().value());
        }
        if (pathParam.isPresent()) {
            return new RestMethodArgument(RestMethodArgumentType.PATH_PARAM, pathParam.get().value());
        }
        if (queryParam.isPresent()) {
            return new RestMethodArgument(RestMethodArgumentType.QUERY_PARAM, queryParam.get().value());
        }
        if (formParam.isPresent()) {
            return new RestMethodArgument(RestMethodArgumentType.FORM_PARAM, formParam.get().value());
        }
        if (isDownloadSink(parameter.getType())) {
            return new RestMethodArgument(RestMethodArgumentType.RESPONSE_SINK, null);
        }
        return new RestMethodArgument(RestMethodArgumentType.BODY, null);
    }

    private boolean isDownloadSink(Class<?> type) {
        if (OutputStream.class.isAssignableFrom(type)) {
            return true;
        }
        if (java.nio.file.Path.class.equals(type) || File.class.equals(type)) {
            return consumes == null;
        }
        return false;
    }

    private void validateArguments() {
        var bodies = Arrays.stream(arguments).filter(a -> a.getType() == RestMethodArgumentType.BODY).count();
        if (bodies > 1) {
            throw new UnsupportedOperationException("more than one body argument on method " + method);
        }
        var hasForm = Arrays.stream(arguments).anyMatch(a -> a.getType() == RestMethodArgumentType.FORM_PARAM);
        if (hasForm && bodies > 0) {
            throw new UnsupportedOperationException("cannot combine @FormParam arguments with a body argument on method " + method);
        }
        var sinks = Arrays.stream(arguments).filter(a -> a.getType() == RestMethodArgumentType.RESPONSE_SINK).count();
        if (sinks > 1) {
            throw new UnsupportedOperationException("more than one response sink argument on method " + method);
        }
    }

    private <T> Optional<T> getAnnotation(Class<T> type, Annotation[] annotations) {
        return Arrays.stream(annotations)
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }

    public Object invoke(RestAdapter restAdapter, String baseUrl, Object[] methodCallArguments) throws Throwable {
        var requestHeaders = new HashMap<String, List<String>>();
        var pathParameters = new HashMap<String, String>();
        var queryParameters = new HashMap<String, List<String>>();
        var formFields = new ArrayList<FormField>();
        Object entity = null;
        var sink = ResponseSink.none();

        if (methodCallArguments != null) {
            for (int i = 0; i < methodCallArguments.length; i++) {
                var argument = arguments[i];
                var value = methodCallArguments[i];
                if (value == null) {
                    continue;
                }
                switch (argument.getType()) {
                    case HEADER_PARAM:
                        for (var item : valuesOf(value)) {
                            requestHeaders.computeIfAbsent(argument.getName(), key -> new ArrayList<>()).add(String.valueOf(item));
                        }
                        break;
                    case PATH_PARAM:
                        pathParameters.put(argument.getName(), String.valueOf(value));
                        break;
                    case QUERY_PARAM:
                        for (var item : valuesOf(value)) {
                            queryParameters.computeIfAbsent(argument.getName(), key -> new ArrayList<>()).add(String.valueOf(item));
                        }
                        break;
                    case FORM_PARAM:
                        formFields.add(new FormField(argument.getName(), value));
                        break;
                    case RESPONSE_SINK:
                        sink = toSink(value);
                        break;
                    case BODY:
                        entity = value;
                        break;
                    default:
                        throw new UnsupportedOperationException("unsupported argument type: " + argument.getType());
                }
            }
        }

        var acceptHeaderPresent = requestHeaders.keySet().stream().anyMatch("accept"::equalsIgnoreCase);
        if (produces != null && !acceptHeaderPresent) {
            requestHeaders.computeIfAbsent("accept", key -> new ArrayList<>()).add(produces);
        }

        var uri = buildUri(baseUrl, pathParameters, queryParameters);
        var charset = MediaTypes.charset(consumes, UTF_8);
        var requestBody = buildRequestBody(restAdapter, formFields, entity, charset);

        var responseSink = sink;
        var temporarySink = false;
        if (!responseSink.isPresent()) {
            responseSink = toTempFileSink();
            temporarySink = responseSink.isPresent();
        }

        try {
            return restAdapter.invoke(requestMethod, uri, requestHeaders, requestBody, returnType, responseSink);
        } catch (Exception ex) {
            if (temporarySink) {
                deleteTemporarySink(responseSink);
            }
            throw ex;
        }
    }

    private void deleteTemporarySink(ResponseSink sink) {
        try {
            if (sink.getKind() == ResponseSink.Kind.PATH) {
                java.nio.file.Files.deleteIfExists(sink.getPath());
            } else if (sink.getKind() == ResponseSink.Kind.FILE) {
                java.nio.file.Files.deleteIfExists(sink.getFile().toPath());
            }
        } catch (java.io.IOException ignored) {
            // best effort cleanup
        }
    }

    private RequestBody buildRequestBody(RestAdapter restAdapter, List<FormField> formFields, Object entity, Charset charset)
            throws java.io.IOException {
        if (!formFields.isEmpty()) {
            if (MediaTypes.matches(consumes, MediaTypes.MULTIPART_FORM_DATA)) {
                return RequestBodyFactory.encodeMultipart(formFields, restAdapter.getJson(), charset);
            }
            return RequestBodyFactory.encodeForm(formFields, charset);
        }
        return RequestBodyFactory.encode(entity, consumes, restAdapter.getJson(), restAdapter.getXml(), charset);
    }

    private ResponseSink toSink(Object value) {
        if (value instanceof java.nio.file.Path) {
            return ResponseSink.toPath((java.nio.file.Path) value);
        }
        if (value instanceof File) {
            return ResponseSink.toFile((File) value);
        }
        if (value instanceof OutputStream) {
            return ResponseSink.toOutputStream((OutputStream) value);
        }
        return ResponseSink.none();
    }

    private ResponseSink toTempFileSink() throws java.io.IOException {
        if (java.nio.file.Path.class.equals(rawReturnType)) {
            return ResponseSink.toPath(java.nio.file.Files.createTempFile("easy-rest-client-", ".download"));
        }
        if (File.class.equals(rawReturnType)) {
            return ResponseSink.toFile(java.nio.file.Files.createTempFile("easy-rest-client-", ".download").toFile());
        }
        return ResponseSink.none();
    }

    private URI buildUri(String baseUrl, Map<String, String> pathParameters, Map<String, List<String>> queryParameters) {
        var uriString = Stream.of(baseUrl, classPath, methodPath)
                .filter(Objects::nonNull)
                .map(StringUtil::removeLeadingAndTrailingSlashes)
                .collect(joining("/"));

        for (String param : pathParameters.keySet()) {
            uriString = uriString.replace("{" + param + "}", pathEncode(pathParameters.get(param)));
        }
        if (!queryParameters.isEmpty()) {
            uriString = uriString + "?" + queryParameters.entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream()
                            .map(value -> urlEncode(entry.getKey()) + "=" + urlEncode(value)))
                    .collect(joining("&"));
        }
        return URI.create(uriString);
    }

    private static <T> T firstNonNull(T first, T second) {
        return first != null ? first : second;
    }

    private static List<?> valuesOf(Object value) {
        if (value instanceof Iterable) {
            var values = new ArrayList<>();
            ((Iterable<?>) value).forEach(values::add);
            return values;
        }
        if (value.getClass().isArray()) {
            var length = java.lang.reflect.Array.getLength(value);
            var values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                values.add(java.lang.reflect.Array.get(value, i));
            }
            return values;
        }
        return List.of(value);
    }

    private static String firstMediaType(Consumes consumes) {
        return consumes != null && consumes.value().length > 0 ? consumes.value()[0] : null;
    }

    private static String firstMediaType(Produces produces) {
        return produces != null && produces.value().length > 0 ? produces.value()[0] : null;
    }

    private enum RestMethodArgumentType {
        HEADER_PARAM("@HeaderParam"),
        PATH_PARAM("@PathParam"),
        QUERY_PARAM("@QueryParam"),
        FORM_PARAM("@FormParam"),
        RESPONSE_SINK("ResponseSink"),
        BODY("Body");

        private String info;

        RestMethodArgumentType(String info) {
            this.info = info;
        }

        @Override
        public String toString() {
            return info;
        }
    }

    private static class RestMethodArgument {
        private final RestMethodArgumentType type;
        private final String name;

        public RestMethodArgument(RestMethodArgumentType type, String name) {
            this.type = type;
            this.name = name;
        }

        public RestMethodArgumentType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name != null ? String.format("%s(\"%s\")", type, name) : type.toString();
        }
    }
}
