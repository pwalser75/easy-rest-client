# Default and static methods

Since Java 8, interfaces can also have `default` and `static` methods. Rest client interfaces only need to provide
JAX-RS annotations for all the _abstract_ interface methods, and can use additional `default` and `static` methods for
more convenient access. These methods run locally and are not sent as HTTP requests.

```java
public interface HelloClient {

    // the abstract interface methods are proxied for REST web calls
    @GET
    @Path("hello/{lang}")
    String hello(@PathParam("lang") String lang, @QueryParam("name") String name);

    // a convenience default method that uses the current user language
    default String hello(String name) {
        return hello(userLanguage(), name);
    }

    // a convenience default method with fixed arguments
    default String helloWorld() {
        return hello("en", "World");
    }

    // static method to determine the current user language
    static String userLanguage() {
        return Locale.getDefault().getLanguage();
    }
}
```

Usage:

```java
var helloClient = RestClient.build(httpClient, baseUrl, HelloClient.class);

helloClient.hello("en", "world"); // HTTP request
helloClient.hello("world");       // default method, delegates to hello(lang, name)
helloClient.helloWorld();         // default method with fixed arguments
```
