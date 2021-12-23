package ch.frostnova.web.eastrestclient.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class RestClientInterface<T> {

    private final Class<T> interfaceClass;
    private final Map<Method, RestMethodAdapter> methodAdapters = new HashMap<>();

    public RestClientInterface(Class<T> interfaceClass) {

        // TODO: scan for REST methods and memorize them (statically, as they are defined by restClientInterface)
        // TODO: create and memorize optimized invocation function (method + arguments -> return type)

        this.interfaceClass = requireNonNull(interfaceClass);
        for (Method method : interfaceClass.getDeclaredMethods()) {
            methodAdapters.put(method, new RestMethodAdapter(method));
        }
    }

    public RestMethodAdapter get(Method method) {
        return Optional.ofNullable(methodAdapters.get(method)).orElseThrow(() -> new NoSuchElementException("Unknown method: " + method));
    }
}
