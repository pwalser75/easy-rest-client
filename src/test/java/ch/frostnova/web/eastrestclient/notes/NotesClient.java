package ch.frostnova.web.eastrestclient.notes;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("api/notes")
public interface NotesClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<Note> list();

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Note get(@PathParam("id") long id);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Note create(Note note);

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    void update(@PathParam("id") long id, Note note);

    @DELETE
    @Path("/{id}")
    void delete(@PathParam("id") long id);
}
