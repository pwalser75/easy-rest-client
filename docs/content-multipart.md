# Multipart form data (`multipart/form-data`)

Multipart requests are declared using the standard JAX-RS annotations only: the request is declared as
`@Consumes(MULTIPART_FORM_DATA)`, and each part is a `@FormParam`.

```java
@Path("api/multipart")
public interface MultipartClient {

    @POST
    @Path("upload")
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    UploadResult upload(@FormParam("title") String title,
                        @FormParam("file") Path file,
                        @FormParam("tag") List<String> tags);
}
```

Part encoding rules:

- **Binary parts** (`Path`, `File`, `InputStream`, `byte[]`, `ByteBuffer`) are streamed. File parts are sent with
  their file name and a media type probed from the file, falling back to `application/octet-stream`.
- **Simple values** (`String`, numbers, booleans, enums) are sent as `text/plain` parts.
- **Complex values** are JSON-encoded and sent with `application/json`.
- A collection or array produces one part per element.

```java
@POST
@Path("upload")
@Consumes(MULTIPART_FORM_DATA)
@Produces(APPLICATION_JSON)
UploadResult uploadWithMeta(@FormParam("title") String title,
                            @FormParam("file") Path file,
                            @FormParam("meta") MetaInfo meta);
```

The multipart body is streamed using a `HttpRequest.BodyPublisher` that concatenates the part headers and content;
the request is sent with chunked transfer encoding because the total content length is not known upfront.
