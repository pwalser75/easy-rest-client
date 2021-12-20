package ch.frostnova.web.eastrestclient.http;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class RestMethodAdapter {

    private final Method method;

    public RestMethodAdapter(Method method) {
        this.method = method;
    }

    public Object invoke(RestAdapter restAdapter, String baseUrl, Object[] methodCallArguments) throws Throwable {
        Class<?> returnType = method.getReturnType();

        Map<String, String> pathParameters = new HashMap<>();
        Map<String, String> queryParameters = new HashMap<>();
        Object body = null;

        Parameter[] parameters = method.getParameters();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
            Annotation[] annotations = parameterAnnotations[parameterIndex];
            for (Annotation annotation : annotations) {
                if (annotation instanceof QueryParam) {
                    QueryParam queryParam = (QueryParam) annotation;
                    queryParameters.put(queryParam.value(), String.valueOf(methodCallArguments[parameterIndex]));
                } else if (annotation instanceof PathParam) {
                    PathParam pathParam = (PathParam) annotation;
                    pathParameters.put(pathParam.value(), String.valueOf(methodCallArguments[parameterIndex]));
                }
            }
            if (annotations.length == 0) {
                // under construction: consider it the request body
                body = methodCallArguments[parameterIndex];
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

        String uriString = Stream.of(baseUrl,
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
            return restAdapter.invoke(RequestMethod.GET, new URI(uriString), consumes, produces, body, returnType);
        }
        if (postRequest != null) {
            return restAdapter.invoke(RequestMethod.POST, new URI(uriString), consumes, produces, body, returnType);
        }
        if (putRequest != null) {
            return restAdapter.invoke(RequestMethod.PUT, new URI(uriString), consumes, produces, body, returnType);
        }
        if (deleteRequest != null) {
            return restAdapter.invoke(RequestMethod.DELETE, new URI(uriString), consumes, produces, body, returnType);
        }
        throw new UnsupportedOperationException("unsupported method, use GET, POST, PUT or DELETE");
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

    private String urlEncode(Object value) {
        if (value == null) {
            return null;
        }
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.ISO_8859_1);
    }
}
