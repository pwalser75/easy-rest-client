package ch.frostnova.web.eastrestclient.weather.backend;

import ch.frostnova.web.eastrestclient.weather.api.Condition;
import ch.frostnova.web.eastrestclient.weather.api.Temperature;
import ch.frostnova.web.eastrestclient.weather.api.WeatherForecast;
import ch.frostnova.web.eastrestclient.weather.api.WeatherForecastDay;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@RestController
@RequestMapping(path = "api/weather")
@CrossOrigin(origins = "*",
        allowedHeaders = "origin, content-type, accept, authorization",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.HEAD},
        maxAge = 1209600)
public class WeatherForecastController {

    @GetMapping(path = "forecast", produces = APPLICATION_XML_VALUE)
    public WeatherForecast get(@RequestParam("location") String location) {

        WeatherForecast weatherForecast = new WeatherForecast();
        weatherForecast.setLocation(location);
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < 5; i++) {
            WeatherForecastDay weatherForecastDay = new WeatherForecastDay();
            weatherForecastDay.setLocalDate(LocalDate.now().plusDays(i));
            weatherForecastDay.setCondition(Condition.values()[random.nextInt(Condition.values().length)]);
            weatherForecastDay.setTemperature(new Temperature(random.nextDouble() * 50 - 18, Temperature.Unit.CELSIUS));
            weatherForecast.add(weatherForecastDay);
        }
        return weatherForecast;
    }
}
