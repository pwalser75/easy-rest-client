package ch.frostnova.web.eastrestclient.weather.api;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("api/weather")
public interface WeatherClient {

    @GET
    @Path("forecast")
    @Produces(MediaType.APPLICATION_XML)
    WeatherForecast getForecast(@HeaderParam("api-key") String apiKey, @QueryParam("location") String location);
}
