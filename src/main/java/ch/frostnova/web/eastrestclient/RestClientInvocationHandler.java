package ch.frostnova.web.eastrestclient;

import ch.frostnova.web.eastrestclient.converter.ObjectMappers;
import ch.frostnova.web.eastrestclient.http.RestAdapter;
import ch.frostnova.web.eastrestclient.http.RestClientInterface;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.http.HttpClient;

import static java.util.Objects.requireNonNull;

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

    private final String baseURL;
    private final RestClientInterface<?> restClientInterface;
    private final RestAdapter restAdapter;

    private RestClientInvocationHandler(HttpClient httpClient, String baseURL, Class<?> restClientInterfaceClass) {
        this.baseURL = baseURL;

        restAdapter = new RestAdapter(httpClient, ObjectMappers.json(), ObjectMappers.xml());
        restClientInterface = new RestClientInterface(restClientInterfaceClass);
    }

    @Override
    public Object invoke(Object o, Method method, Object[] arguments) throws Throwable {
        return restClientInterface.get(method).invoke(restAdapter, baseURL, arguments);
    }
}