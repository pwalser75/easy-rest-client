package ch.frostnova.web.eastrestclient.http;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class RestClientInterface<T> {

    private final Map<Method, RestMethodAdapter> methodAdapters = new HashMap<>();

    public RestClientInterface(Class<T> interfaceClass) {
        requireNonNull(interfaceClass);
        for (Method method : interfaceClass.getDeclaredMethods()) {
            // ignore default and static interface methods
            if (!method.isDefault() && !Modifier.isStatic(method.getModifiers())) {
                methodAdapters.put(method, new RestMethodAdapter(method));
            }
        }
    }

    public RestMethodAdapter get(Method method) {
        return Optional.ofNullable(methodAdapters.get(method)).orElseThrow(() -> new NoSuchElementException("Unknown method: " + method));
    }
}
