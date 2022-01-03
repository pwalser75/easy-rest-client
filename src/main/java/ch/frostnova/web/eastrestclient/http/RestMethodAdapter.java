package ch.frostnova.web.eastrestclient.http;

import ch.frostnova.web.eastrestclient.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static ch.frostnova.web.eastrestclient.util.StringUtil.urlEncode;
import static java.util.stream.Collectors.joining;

public class RestMethodAdapter {

    private Logger logger = LoggerFactory.getLogger(RestMethodAdapter.class);

    private final Method method;
    private final Type returnType;
    private final RequestMethod requestMethod;
    private final RestMethodArgument[] arguments;

    public RestMethodAdapter(Method method) {
        this.method = method;
        returnType = method.getGenericReturnType();

        requestMethod = determineRequestMethod(method);

        Parameter[] parameters = method.getParameters();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        arguments = new RestMethodArgument[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            arguments[i] = toArgument(i, parameterAnnotations[i]);
        }
        logger.debug("bound @{} {}.{}({}) -> {}", requestMethod,
                method.getDeclaringClass().getSimpleName(), method.getName(),
                Arrays.stream(arguments).map(String::valueOf).collect(joining(", ")), method.getGenericReturnType());
    }

    private RequestMethod determineRequestMethod(Method method) {
        GET getRequest = method.getAnnotation(GET.class);
        POST postRequest = method.getAnnotation(POST.class);
        PUT putRequest = method.getAnnotation(PUT.class);
        DELETE deleteRequest = method.getAnnotation(DELETE.class);

        long matchingAnnotations = Stream.of(getRequest, postRequest, putRequest, deleteRequest).filter(Objects::nonNull).count();
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

    private RestMethodArgument toArgument(int index, Annotation[] annotations) {
        Optional<HeaderParam> headerParam = getAnnotation(HeaderParam.class, annotations);
        Optional<PathParam> pathParam = getAnnotation(PathParam.class, annotations);
        Optional<QueryParam> queryParam = getAnnotation(QueryParam.class, annotations);
        Optional<FormParam> formParam = getAnnotation(FormParam.class, annotations);

        long matchingAnnotations = Stream.of(headerParam, pathParam, queryParam, formParam).filter(Optional::isPresent).count();
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
        return new RestMethodArgument(RestMethodArgumentType.BODY, null);
    }

    private <T> Optional<T> getAnnotation(Class<T> type, Annotation[] annotations) {
        return Arrays.stream(annotations)
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }

    public Object invoke(RestAdapter restAdapter, String baseUrl, Object[] methodCallArguments) throws Throwable {
        Map<String, String> requestHeaders = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        Map<String, String> queryParameters = new HashMap<>();
        Object body = null;

        if (methodCallArguments != null) {
            for (int i = 0; i < methodCallArguments.length; i++) {
                RestMethodArgument argument = arguments[i];
                Object value = methodCallArguments[i];
                if (value != null) {
                    if (argument.getType() == RestMethodArgumentType.HEADER_PARAM) {
                        requestHeaders.put(argument.getName(), String.valueOf(value));
                    }
                    if (argument.getType() == RestMethodArgumentType.PATH_PARAM) {
                        pathParameters.put(argument.getName(), String.valueOf(value));
                    }
                    if (argument.getType() == RestMethodArgumentType.QUERY_PARAM) {
                        queryParameters.put(argument.getName(), String.valueOf(value));
                    }
                    if (argument.getType() == RestMethodArgumentType.FORM_PARAM) {
                        // TODO
                    }
                    if (argument.getType() == RestMethodArgumentType.BODY) {
                        body = value;
                    }
                }
            }
        }

        Path classUriPath = method.getDeclaringClass().getAnnotation(Path.class);
        Path methodUriPath = method.getAnnotation(Path.class);

        Consumes consumes = method.getAnnotation(Consumes.class);
        Produces produces = method.getAnnotation(Produces.class);

        String uriString = Stream.of(baseUrl,
                        Optional.ofNullable(classUriPath).map(Path::value).orElse(null),
                        Optional.ofNullable(methodUriPath).map(Path::value).orElse(null)
                )
                .filter(Objects::nonNull)
                .map(StringUtil::removeLeadingAndTrailingSlashes)
                .collect(joining("/"));

        // replace path parameters
        // TODO: String conversion of parameters: non-trivial types, collections, ...
        for (String param : pathParameters.keySet()) {
            uriString = uriString.replace("{" + param + "}", urlEncode(pathParameters.get(param)));
        }

        if (!queryParameters.isEmpty()) {
            uriString = uriString + "?" + queryParameters.entrySet().stream()
                    .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
                    .collect(joining("&"));
        }

        // TODO: check if exactly one request type is present, otherwise throw an exception.
        // TODO: check if the path is present, otherwise throw an exception.
        // TODO: also consider path as class annotation (counts as base plus optional method path)

        return restAdapter.invoke(requestMethod, new URI(uriString), requestHeaders, consumes, produces, body, returnType);
    }

    private enum RestMethodArgumentType {
        HEADER_PARAM("@HeaderParam"),
        PATH_PARAM("@PathParam"),
        QUERY_PARAM("@QueryParam"),
        FORM_PARAM("@FormParam"),
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
