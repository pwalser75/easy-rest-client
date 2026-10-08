# Interface validation

Client interfaces are validated when they are first scanned (cached per interface). Invalid declarations fail fast
with an `UnsupportedOperationException`.

| Invalid declaration | Error |
| --- | --- |
| Method without `@GET`/`@POST`/`@PUT`/`@DELETE` | `no request method annotation found on method ...` |
| Method with more than one request method annotation | `multiple request method annotations found on method ...` |
| Parameter with more than one binding annotation | `more than one param annotation on argument ...` |
| More than one body argument | `more than one body argument on method ...` |
| `@FormParam` combined with a body argument | `cannot combine @FormParam arguments with a body argument on method ...` |
| More than one response sink argument | `more than one response sink argument on method ...` |

Example of an invalid interface:

```java
public interface InvalidClient {

    // no request method annotation
    @Path("x")
    String invalid();
}
```

Building a client for such an interface throws an `UnsupportedOperationException` describing the problem.
