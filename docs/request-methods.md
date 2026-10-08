# HTTP methods and paths

A client interface method must carry exactly one request method annotation (`@GET`, `@POST`, `@PUT` or `@DELETE`).
The request path is composed of the (optional) class-level `@Path`, the (optional) method-level `@Path`, and the
base URL passed to `RestClient.build(...)`. Leading and trailing slashes are normalized.

```java
@Path("api/notes")
public interface NotesClient {

    @GET
    List<Note> list();                       // GET  {base}/api/notes

    @GET
    @Path("/{id}")
    Note get(@PathParam("id") long id);       // GET  {base}/api/notes/{id}

    @POST
    Note create(Note note);                  // POST {base}/api/notes

    @PUT
    @Path("/{id}")
    void update(@PathParam("id") long id, Note note); // PUT {base}/api/notes/{id}

    @DELETE
    @Path("/{id}")
    void delete(@PathParam("id") long id);   // DELETE {base}/api/notes/{id}
}
```

## Request bodies

A request body is sent for `@POST` and `@PUT`. `@GET` and `@DELETE` do not send a body, even if a body argument is
declared. Encodings of the body depend on the declared `@Consumes` media type, see the content type examples.

## Rules

- A method without a request method annotation, or with more than one, is rejected when the client interface is
  scanned (an `UnsupportedOperationException` is thrown).
- Only `GET`, `POST`, `PUT` and `DELETE` are supported.
