# XML (`application/xml`)

XML bodies are declared with `@Consumes(APPLICATION_XML)` / `@Produces(APPLICATION_XML)` and are serialized by
Jackson's `XmlMapper`.

```java
@Path("api/content")
public interface ContentClient {

    @POST
    @Path("xml")
    @Consumes(APPLICATION_XML)
    @Produces(APPLICATION_XML)
    ContentEcho echoXml(ContentEcho value);
}
```

The mapper is configured with `JacksonXmlModule` and `setDefaultUseWrapper(false)`, so collections are written
without a wrapping element. Jackson XML annotations can be used to control element names, e.g.:

```java
@JacksonXmlRootElement(localName = "content-echo")
@JsonPropertyOrder({"name", "value", "tag"})
public class ContentEcho {

    @JacksonXmlProperty(localName = "name")
    private String name;

    @JacksonXmlElementWrapper(localName = "tag", useWrapping = false)
    private List<String> tags = new ArrayList<>();
}
```

As for JSON, `@Produces` is optional on the client: the response `Content-Type` header decides whether the response
is read as XML or JSON.
