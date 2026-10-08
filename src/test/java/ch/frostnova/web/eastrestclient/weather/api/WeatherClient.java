package ch.frostnova.web.eastrestclient.weather.api;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("api/weather")
public interface WeatherClient {

    @GET
    @Path("forecast")
    WeatherForecast getForecast(@HeaderParam("api-key") String apiKey,
                                @QueryParam("location") String location);

    @DELETE
    @Path("forecast")
    void deleteForecast();
}
