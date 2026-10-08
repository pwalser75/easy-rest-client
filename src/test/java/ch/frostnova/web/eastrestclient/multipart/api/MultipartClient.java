package ch.frostnova.web.eastrestclient.multipart.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("api/multipart")
public interface MultipartClient {

    @POST
    @Path("upload")
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    UploadResult upload(@FormParam("title") String title,
                        @FormParam("file") java.nio.file.Path file,
                        @FormParam("tag") List<String> tags);

    @POST
    @Path("upload-with-meta")
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    UploadResult uploadWithMeta(@FormParam("title") String title,
                                @FormParam("file") java.nio.file.Path file,
                                @FormParam("meta") MetaInfo meta);
}
