# Form data (`application/x-www-form-urlencoded`)

Form parameters are sent as `application/x-www-form-urlencoded` content by declaring
`@Consumes(APPLICATION_FORM_URLENCODED)` and binding each field with `@FormParam`.

```java
@Path("api/forms")
public interface FormClient {

    @POST
    @Path("echo")
    @Consumes(APPLICATION_FORM_URLENCODED)
    @Produces(APPLICATION_JSON)
    FormData echo(@FormParam("name") String name,
                  @FormParam("age") int age,
                  @FormParam("tag") List<String> tags);
}
```

A parameter of a collection or array type is encoded as a repeated form field:

```java
formClient.echo("Äphry von Üetliberg", 42, List.of("alpha", "beta", "gamma"));
// body: name=%C3%84phry+von+%C3%9Cetliberg&age=42&tag=alpha&tag=beta&tag=gamma
```

A `@FormParam` argument cannot be combined with a body argument—the form fields are the request body. The response
may be any other content type (here JSON).
