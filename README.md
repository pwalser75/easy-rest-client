# Easy Rest Client

:construction: **INCUBATION PROJECT** - :warning: *NOT FUNCTIONAL YET*

**A new approach on writing REST web service clients using declarative interfaces with JAX-RS annotations.**

## Idea

This project was inspired by the way *Spring Data Repositories* are used: an interface serves as a contract, and the
actual implementation is a Java Proxy backed by an Invocation Handler which provides the implementation dynamically
based on the contract.

The idea for the Easy Rest Client is as follows:

- For a rest client, an **interface** defining the connecting endpoint is defined, using web service **annotations**.
- A **factory** method creates an implementation of a rest client, given that interface, a base URL and a web client.
- The **implementation** will be a `Proxy` backed by a `InvocationHandler` (package: `java.lang.reflect`)

## Technology choices

- **Web Service Annotations** for the REST client: **JAX-RS API**. <br>
  Reason: simple API, lightweight, and considered a standard (Java EE) and interoperable (Spring Web annotations are too
  Spring-centric).
- **HTTP Client**: `java.net.http.HttpClient` (built-in since Java 11). <br>
  Reason: available in Java standard Library, so no additional libraries are required.

## JAX-RS Annotation Support

The following JAX-RS annotiations (package: `javax.ws.rs`) are supported:

- [ ] `@GET`
- [ ] `@POST`
- [ ] `@PUT`
- [ ] `@DELETE`
- [ ] `@Consumes`
- [ ] `@Produces`
- [ ] `@PathParam`
- [ ] `@QueryParam`
- [ ] `@FormParam`
- [ ] `@HeaderParam`

## Example Usage

### GET example

Let's write a REST client for the following endpoint: <br>
`GET https://test.org/hello/{lang}?name={name}`

This endpoint has path parameter for the language (ISO code) and expects a name using a query parameter. It would
respond with a plain text message, greeting the user by name in the selected language. The appropriate interface would
look like this:

```java
public interface HelloClient {
    @GET
    @Path("hello/{lang}")
    @Produces(MediaType.TEXT_PLAIN)
    String hello(@PathParam("lang") String lang, @QueryParam("name") String name);
}
```

The instance for this client would be created as follows:

```java
HttpClient httpClient = HttpClient.newBuilder().build();
String baseUrl = "https://test.org";

HelloClient helloClient = RestClient.build(httpClient, baseUrl, HelloClient.class);
```

Using the client is then plain simple:

```java
String message = helloClient.hello("en", "Alastor Moody");
```

### POST example

*TODO*

## Build

Build with Gradle Wrapper:

```bsh
./gradlew
```