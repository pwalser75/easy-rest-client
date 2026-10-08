# JSON (`application/json`)

JSON is the default content type. It is used both for request and response bodies, for single objects as well as
collections.

```java
@Path("api/notes")
public interface NotesClient {

    @GET
    @Produces(APPLICATION_JSON)
    List<Note> list();

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    Note get(@PathParam("id") long id);

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    Note create(Note note);
}
```

The serialization is performed by Jackson (`com.fasterxml.jackson.databind.ObjectMapper`), with the JSR-310 module
registered for `java.time` types.

## `@Produces` is optional on the client

The deserialization of a structured response is driven by the **response** `Content-Type` header, not by the declared
`@Produces`. When a method returns a structured value and no `@Produces` is declared, JSON is assumed unless the
response says otherwise.

```java
@GET
@Path("/{id}")
Note get(@PathParam("id") long id); // content type of the response decides JSON vs. XML
```

## Default body encoding

If a body argument is present and no `@Consumes` media type is declared, the body is serialized as JSON:

```java
@POST
ContentEcho echo(ContentEcho value); // sent as application/json
```
