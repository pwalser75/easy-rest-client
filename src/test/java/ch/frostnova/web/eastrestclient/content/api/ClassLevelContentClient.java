package ch.frostnova.web.eastrestclient.content.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Client interface declaring the media types on the interface instead of on each method.
 */
@Path("api/content")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface ClassLevelContentClient {

    @POST
    @Path("json")
    ContentEcho echo(ContentEcho value);
}
