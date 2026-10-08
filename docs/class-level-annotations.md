# Class-level annotations

`@Path`, `@Consumes` and `@Produces` may be placed on the client interface. A method-level annotation takes
precedence over the class-level annotation of the same kind.

```java
@Path("api/content")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface ClassLevelContentClient {

    @POST
    @Path("json")
    ContentEcho echo(ContentEcho value); // uses class-level consumes/produces
}
```

This is equivalent to declaring `@Consumes(APPLICATION_JSON)` and `@Produces(APPLICATION_JSON)` on every method, and
keeps the interface concise when all methods share the same media types.

Class-level `@Path` is always combined with the method-level `@Path` to form the request path.
