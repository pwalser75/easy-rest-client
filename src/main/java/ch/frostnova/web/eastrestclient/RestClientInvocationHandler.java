package ch.frostnova.web.eastrestclient;

import java.net.http.HttpClient;

public class RestClientInvocationHandler {

    public static <T> T build(HttpClient httpClient, String baseURL, Class<T> restClientInterface) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
