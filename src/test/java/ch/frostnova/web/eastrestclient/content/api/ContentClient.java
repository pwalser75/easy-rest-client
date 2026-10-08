package ch.frostnova.web.eastrestclient.content.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("api/content")
public interface ContentClient {

    @POST
    @Path("json")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    ContentEcho echoJson(ContentEcho value);

    @POST
    @Path("xml")
    @Consumes(APPLICATION_XML)
    @Produces(APPLICATION_XML)
    ContentEcho echoXml(ContentEcho value);

    @POST
    @Path("text")
    @Consumes(TEXT_PLAIN)
    @Produces(TEXT_PLAIN)
    String echoText(String value);

    @POST
    @Path("default")
    @Produces(APPLICATION_JSON)
    ContentEcho echoDefault(ContentEcho value);

    @GET
    @Path("text/{name}")
    @Produces(TEXT_PLAIN)
    String greeting(@PathParam("name") String name);

    @GET
    @Path("search")
    SearchResult search(@QueryParam("q") String query,
                        @QueryParam("tag") List<String> tags,
                        @HeaderParam("X-Tag") List<String> headerTags);

    @GET
    @Path("forbidden")
    @Produces(APPLICATION_JSON)
    void forbidden();

    @GET
    @Path("not-acceptable")
    @Produces(APPLICATION_JSON)
    String notAcceptable();

    @POST
    @Path("unsupported-media")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    ContentEcho unsupportedMedia(ContentEcho value);

    @GET
    @Path("server-error")
    @Produces(APPLICATION_JSON)
    void serverError();

    @GET
    @Path("unavailable")
    @Produces(APPLICATION_JSON)
    void unavailable();

    @GET
    @Path("teapot")
    @Produces(APPLICATION_JSON)
    void teapot();

    @GET
    @Path("bad-gateway")
    @Produces(APPLICATION_JSON)
    void badGateway();
}
