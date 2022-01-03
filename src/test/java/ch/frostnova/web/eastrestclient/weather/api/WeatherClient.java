package ch.frostnova.web.eastrestclient.weather.api;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

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
