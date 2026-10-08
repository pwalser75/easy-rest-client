# Plain text (`text/plain`)

Text request and response bodies are declared with `@Consumes(TEXT_PLAIN)` / `@Produces(TEXT_PLAIN)`. The body is
encoded using UTF-8 (or the charset from the `Content-Type` header when present).

```java
@Path("api/content")
public interface ContentClient {

    @GET
    @Path("greeting/{name}")
    @Produces(TEXT_PLAIN)
    String greeting(@PathParam("name") String name);

    @POST
    @Path("echo")
    @Consumes(TEXT_PLAIN)
    @Produces(TEXT_PLAIN)
    String echo(String text);
}
```

Any `CharSequence` body is sent as `text/plain` even when no `@Consumes` is declared:

```java
@POST
String echo(String text); // sent as text/plain
```

When the response `Content-Type` is any `text/*` type and the return type is `String`, the body is returned as a
string.
