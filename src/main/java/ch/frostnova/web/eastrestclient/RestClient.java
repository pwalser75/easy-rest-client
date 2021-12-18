package ch.frostnova.web.eastrestclient;

import java.net.http.HttpClient;

/**
 * Rest client builder, creates instances for JAX-RS-annotated rest client interfaces.
 *
 * @author pwalser@frostnova.ch
 * @since 2021-12-18
 */
public class RestClient {

    public static <T> T build(HttpClient httpClient, String baseURL, Class<T> restClientInterface) {
        return RestClientInvocationHandler.create(httpClient, baseURL, restClientInterface);
    }
}
