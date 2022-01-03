package ch.frostnova.web.eastrestclient;

import ch.frostnova.web.eastrestclient.converter.ObjectMappers;
import ch.frostnova.web.eastrestclient.http.RestAdapter;
import ch.frostnova.web.eastrestclient.http.RestClientInterface;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

class RestClientInvocationHandler implements InvocationHandler {

    private final Class<?> restClientInterfaceClass;
    private final static Map<Class<?>, RestClientInterface> knownRestClientInterfaces = new HashMap<>();
    private final String baseURL;
    private final RestClientInterface<?> restClientInterface;
    private final RestAdapter restAdapter;

    private RestClientInvocationHandler(HttpClient httpClient, String baseURL, Class<?> restClientInterfaceClass) {
        this.restClientInterfaceClass = restClientInterfaceClass;
        this.baseURL = baseURL;

        restAdapter = new RestAdapter(httpClient, ObjectMappers.json(), ObjectMappers.xml());
        restClientInterface = knownRestClientInterfaces.computeIfAbsent(restClientInterfaceClass, RestClientInterface::new);
    }

    public static <T> T create(HttpClient httpClient, String baseURL, Class<T> restClientInterface) {
        requireNonNull(httpClient, "httpClient is required");
        requireNonNull(baseURL, "baseURL is required");
        requireNonNull(restClientInterface, "restClientInterface is required");

        return (T) Proxy.newProxyInstance(
                restClientInterface.getClassLoader(),
                new Class[]{restClientInterface},
                new RestClientInvocationHandler(httpClient, baseURL, restClientInterface));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
        if (method.isDefault()) {
            return MethodHandles.lookup()
                    .findSpecial(restClientInterfaceClass, method.getName(),
                            MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
                            restClientInterfaceClass)
                    .bindTo(proxy)
                    .invokeWithArguments(arguments);
        }
        return restClientInterface.get(method).invoke(restAdapter, baseURL, arguments);
    }
}