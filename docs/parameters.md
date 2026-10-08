# Parameters

Besides the request body, method parameters can be bound with `@PathParam`, `@QueryParam`, `@HeaderParam` and
`@FormParam`. A parameter without any of these annotations is treated as the request body.

```java
@Path("api/weather")
public interface WeatherClient {

    @GET
    @Path("forecast/{region}")
    WeatherForecast getForecast(@PathParam("region") String region,
                                @QueryParam("location") String location,
                                @HeaderParam("api-key") String apiKey);
}
```

## @PathParam

Replaces a `{name}` placeholder in the composed path. Values are URL-encoded.

```java
@GET
@Path("notes/{id}")
Note get(@PathParam("id") long id);
```

## @QueryParam

Appends a query parameter. Null values are omitted.

```java
@GET
WeatherForecast getForecast(@QueryParam("location") String location);
// -> GET .../forecast?location=Winterthur
```

A parameter of a collection or array type is repeated:

```java
@GET
List<Note> search(@QueryParam("tag") List<String> tags);
// tags = [a, b] -> GET .../search?tag=a&tag=b
```

## @HeaderParam

Adds a request header. Null values are omitted.

```java
@GET
WeatherForecast getForecast(@HeaderParam("api-key") String apiKey);
```

A parameter of a collection or array type is repeated, resulting in a header with multiple values:

```java
@GET
void list(@HeaderParam("Accept-Language") List<String> languages);
```

## @FormParam

Binds a parameter to a form field (used with `application/x-www-form-urlencoded` or `multipart/form-data`), see
[Form data](content-form.md) and [Multipart form data](content-multipart.md).

## Body parameter

A parameter without a parameter annotation is encoded as the request body. Only one body argument is allowed, and it
cannot be combined with `@FormParam` arguments.

```java
@POST
@Consumes(APPLICATION_JSON)
Note create(Note note);
```

## Response sink

A special trailing parameter of type `OutputStream`, `Path` or `File` (for downloads without a `@Consumes` media
type) is used as the target for a streamed response, see [Binary content](content-binary.md).

## Rules

- A parameter must have at most one binding annotation (e.g. `@PathParam` and `@QueryParam` together are rejected).
- At most one body argument and at most one response sink argument are allowed.
- `@FormParam` arguments cannot be combined with a body argument.
