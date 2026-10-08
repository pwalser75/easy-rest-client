# Binary content and streaming

To avoid holding file contents in memory, both requests and responses are streamed.

## Request body

A body parameter may be of type `byte[]`, `ByteBuffer`, `Path`, `File`, `InputStream`,
`HttpRequest.BodyPublisher`, or `Consumer<OutputStream>`.

```java
@Path("api/files")
public interface FileClient {

    @POST
    @Path("upload")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_JSON)
    FileInfo uploadFromPath(@HeaderParam("X-Filename") String filename, Path file);

    @POST
    @Path("upload")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_JSON)
    FileInfo uploadFromBytes(@HeaderParam("X-Filename") String filename, byte[] content);

    @POST
    @Path("upload")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_JSON)
    FileInfo uploadFromStream(@HeaderParam("X-Filename") String filename, InputStream content);

    @POST
    @Path("upload")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_JSON)
    FileInfo uploadFromPublisher(@HeaderParam("X-Filename") String filename, HttpRequest.BodyPublisher publisher);

    @POST
    @Path("upload")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_JSON)
    FileInfo uploadFromWriter(@HeaderParam("X-Filename") String filename, Consumer<OutputStream> writer);
}
```

The `Consumer<OutputStream>` variant lets the caller write the request body through a callback. The data is piped to
the HTTP client through a background thread, so it is not buffered in memory.

## Response body

A response sink parameter of type `Path`, `File` or `OutputStream` streams the response to it. (A `Path` or `File`
parameter is only treated as a sink when the method has no `@Consumes` declaration—otherwise it is a request body.)

```java
@GET
@Path("{name}")
void download(@PathParam("name") String name, OutputStream target);

@GET
@Path("{name}")
void downloadToFile(@PathParam("name") String name, File target);
```

Alternatively, a method may return `Path` or `File`, in which case the response is streamed to a temporary file which
is returned (the caller is responsible for deleting it):

```java
@GET
@Path("{name}")
Path downloadToPath(@PathParam("name") String name);

@GET
@Path("{name}")
File downloadToFile(@PathParam("name") String name);
```

If a streaming request fails with an error status (4xx/5xx), the error is handled as usual (see
[Error handling](error-handling.md)) and the sink is left empty.
