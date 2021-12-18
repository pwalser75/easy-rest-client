package ch.frostnova.web.eastrestclient;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
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
import java.util.HashMap;
import java.util.Map;
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

        Parameter[] parameters = method.getParameters();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
            Parameter parameter = parameters[parameterIndex];
            Annotation[] annotations = parameterAnnotations[parameterIndex];
            for (Annotation annotation : annotations) {
                if (annotation instanceof QueryParam) {
                    QueryParam queryParam = (QueryParam) annotation;
                    queryParameters.put(queryParam.value(), String.valueOf(objects[parameterIndex]));
                }
                if (annotation instanceof PathParam) {
                    PathParam pathParam = (PathParam) annotation;
                    pathParameters.put(pathParam.value(), String.valueOf(objects[parameterIndex]));
                }
            }
        }

        GET getRequest = method.getAnnotation(GET.class);
        POST postRequest = method.getAnnotation(POST.class);
        PUT putRequest = method.getAnnotation(PUT.class);
        DELETE deleteRequest = method.getAnnotation(DELETE.class);

        Path path = method.getAnnotation(Path.class);
        String pathString = path.value();

        // replace path parameters
        // TODO: String conversion of parameters: non-trivial types, collections, ...
        for (String param : pathParameters.keySet()) {
            pathString = pathString.replace("{" + param + "}", urlEncode(pathParameters.get(param)));
        }

        String uriString = Stream.of(baseURL, pathString).map(this::removeTrailingSlashes).collect(joining("/"));
        if (!queryParameters.isEmpty()) {
            uriString = uriString + "?" + queryParameters.entrySet().stream()
                    .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
                    .collect(joining("&"));
        }

        // TODO: check if exactly one request type is present, otherwise throw an exception.
        // TODO: check if the path is present, otherwise throw an exception.
        // TODO: also consider path as class annotation (counts as base plus optional method path)

        if (getRequest != null) {
            return get(new URI(uriString), returnType);
        }
        throw new UnsupportedOperationException("not supported yet");
    }

    // TODO: consider consumes/produces (currently only supports plain text)
    private <T> T get(URI uri,
                      Class<T> returnType) throws IOException, InterruptedException {

        System.out.println("GET " + uri + " -> " + returnType.getSimpleName());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponseStatus(response);
        String plain = response.body();

        if (String.class.isAssignableFrom(returnType)) {
            return (T) plain;
        }
        // TODO: JSON / XML deserialization
        throw new UnsupportedOperationException("only plain text supported yet");
    }

    private void checkResponseStatus(HttpResponse<?> response) {
        int status = response.statusCode();
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

    private String removeTrailingSlashes(String s) {
        if (s == null) {
            return null;
        }
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}