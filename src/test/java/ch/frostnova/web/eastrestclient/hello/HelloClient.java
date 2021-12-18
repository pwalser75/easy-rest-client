package ch.frostnova.web.eastrestclient.hello;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

public interface HelloClient {

    @GET
    @Path("hello/{lang}")
    @Produces(MediaType.TEXT_PLAIN)
    String hello(@PathParam("lang") String lang,
                 @QueryParam("name") String name);
}