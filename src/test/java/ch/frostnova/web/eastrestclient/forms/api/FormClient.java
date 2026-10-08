package ch.frostnova.web.eastrestclient.forms.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

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
