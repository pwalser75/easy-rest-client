package ch.frostnova.web.eastrestclient.files.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

@Path("api/files")
public interface FileClient {

    @GET
    @Path("{name}")
    void download(@PathParam("name") String name, OutputStream target);

    @GET
    @Path("{name}")
    java.nio.file.Path downloadToPath(@PathParam("name") String name);

    @GET
    @Path("{name}")
    void downloadToFile(@PathParam("name") String name, File target);

    @GET
    @Path("{name}")
    File downloadToFile(@PathParam("name") String name);

    @POST
    @Path("upload")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_JSON)
    FileInfo upload(@HeaderParam("X-Filename") String filename, java.nio.file.Path file);

    @POST
    @Path("upload")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_JSON)
    FileInfo uploadBytes(@HeaderParam("X-Filename") String filename, byte[] content);

    @POST
    @Path("upload")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_JSON)
    FileInfo uploadBuffer(@HeaderParam("X-Filename") String filename, ByteBuffer content);

    @POST
    @Path("upload")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_JSON)
    FileInfo uploadInputStream(@HeaderParam("X-Filename") String filename, InputStream content);

    @POST
    @Path("upload")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_JSON)
    FileInfo uploadPublisher(@HeaderParam("X-Filename") String filename, HttpRequest.BodyPublisher content);

    @POST
    @Path("upload")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_JSON)
    FileInfo uploadStream(@HeaderParam("X-Filename") String filename, Consumer<OutputStream> writer);
}
