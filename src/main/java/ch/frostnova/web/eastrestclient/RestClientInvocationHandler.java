package ch.frostnova.web.eastrestclient;

import ch.frostnova.web.eastrestclient.http.RequestMethod;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

class RestClientInvocationHandler implements InvocationHandler {

    public static <T> T create(HttpClient httpClient, String baseURL, Class<T> restClientInterface) {
        requireNonNull(httpClient, "httpClient is required");
        requireNonNull(baseURL, "baseURL is required");
        requireNonNull(restClientInterface, "restClientInterface is required");

        T proxy = (T) Proxy.newProxyInstance(
                restClientInterface.getClassLoader(),
                new Class[]{restClientInterface},
                new RestClientInvocationHandler(httpClient, baseURL, restClientInterface));
        return proxy;
    }

    private final HttpClient httpClient;
    private final String baseURL;
    private final Class<?> restClientInterface;

    private final ObjectMapper json = ObjectMappers.json();

    private RestClientInvocationHandler(HttpClient httpClient, String baseURL, Class<?> restClientInterface) {
        this.httpClient = httpClient;
        this.baseURL = baseURL;
        this.restClientInterface = restClientInterface;

        // TODO: scan for REST methods and memorize them (statically, as they are defined by restClientInterface)
        // TODO: create and memorize optimized invocation function (method + arguments -> return type)
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        Class<?> returnType = method.getReturnType();

        Map<String, String> pathParameters = new HashMap<>();
        Map<String, String> queryParameters = new HashMap<>();
        Object body = null;

        Parameter[] parameters = method.getParameters();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
            Parameter parameter = parameters[parameterIndex];
            Annotation[] annotations = parameterAnnotations[parameterIndex];
            for (Annotation annotation : annotations) {
                if (annotation instanceof QueryParam) {
                    QueryParam queryParam = (QueryParam) annotation;
                    queryParameters.put(queryParam.value(), String.valueOf(objects[parameterIndex]));
                } else if (annotation instanceof PathParam) {
                    PathParam pathParam = (PathParam) annotation;
                    pathParameters.put(pathParam.value(), String.valueOf(objects[parameterIndex]));
                }
            }
            if (annotations.length == 0) {
                // under construction: consider it the request body
                body = objects[parameterIndex];
            }
        }
        System.out.println("BODY: " + body);

        GET getRequest = method.getAnnotation(GET.class);
        POST postRequest = method.getAnnotation(POST.class);
        PUT putRequest = method.getAnnotation(PUT.class);
        DELETE deleteRequest = method.getAnnotation(DELETE.class);

        Path classUriPath = method.getDeclaringClass().getAnnotation(Path.class);
        Path methodUriPath = method.getAnnotation(Path.class);

        Consumes consumes = method.getAnnotation(Consumes.class);
        Produces produces = method.getAnnotation(Produces.class);

        String uriString = Stream.of(baseURL,
                        Optional.ofNullable(classUriPath).map(Path::value).orElse(null),
                        Optional.ofNullable(methodUriPath).map(Path::value).orElse(null)
                )
                .filter(Objects::nonNull)
                .map(this::removeLeadingAndTrailingSlashes)
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

        if (getRequest != null) {
            return invoke(RequestMethod.GET, new URI(uriString), consumes, produces, body, returnType);
        }
        if (postRequest != null) {
            return invoke(RequestMethod.POST, new URI(uriString), consumes, produces, body, returnType);
        }
        if (putRequest != null) {
            return invoke(RequestMethod.PUT, new URI(uriString), consumes, produces, body, returnType);
        }
        if (deleteRequest != null) {
            return invoke(RequestMethod.DELETE, new URI(uriString), consumes, produces, body, returnType);
        }
        throw new UnsupportedOperationException("unsupported method, use GET, POST, PUT or DELETE");
    }

    private <B, T> T invoke(RequestMethod method, URI uri,
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

    private String urlEncode(Object value) {
        if (value == null) {
            return null;
        }
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.ISO_8859_1);
    }

    private String removeLeadingAndTrailingSlashes(String s) {
        if (s == null) {
            return null;
        }
        while (s.startsWith("/")) {
            s = s.substring(1);
        }
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}