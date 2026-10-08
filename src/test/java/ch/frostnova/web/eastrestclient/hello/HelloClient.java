package ch.frostnova.web.eastrestclient.hello;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import java.util.Locale;

public interface HelloClient {

    @GET
    @Path("hello/{lang}")
    String hello(@PathParam("lang") String lang, @QueryParam("name") String name);

    default String hello(String name) {
        return hello(userLanguage(), name);
    }

    default String helloWorld() {
        return hello("en", "World");
    }

    static String userLanguage() {
        return Locale.getDefault().getLanguage();
    }
}