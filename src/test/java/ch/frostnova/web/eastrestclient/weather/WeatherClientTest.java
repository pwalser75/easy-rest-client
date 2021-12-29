package ch.frostnova.web.eastrestclient.weather;


import ch.frostnova.web.eastrestclient.RestClient;
import ch.frostnova.web.eastrestclient.weather.api.WeatherClient;
import ch.frostnova.web.eastrestclient.weather.api.WeatherForecast;
import ch.frostnova.web.eastrestclient.weather.api.WeatherForecastDay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.http.HttpClient;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Weather client test
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WeatherClientTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @LocalServerPort
    private int port;

    private WeatherClient weatherClient;

    @BeforeEach
    void init() {
        String baseUrl = String.format("http://localhost:%d/", port);
        log.info("BASE URL: " + baseUrl);

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();

        weatherClient = RestClient.build(httpClient, baseUrl, WeatherClient.class);
    }

    @Test
    void shouldGetWeatherForecast() {

        WeatherForecast weatherForecast = weatherClient.getForecast("Winterthur");

        assertThat(weatherForecast).isNotNull();
        assertThat(weatherForecast.getLocation()).isEqualTo("Winterthur");
        assertThat(weatherForecast.getDays()).hasSize(5);
        assertThat(weatherForecast.getDays()).allSatisfy(day -> {
            assertThat(day.getLocalDate()).isNotNull();
            assertThat(day.getCondition()).isNotNull();
            assertThat(day.getTemperature()).isNotNull();
        });

        System.out.println(weatherForecast.getLocation());
        for (WeatherForecastDay weatherForecastDay : weatherForecast.getDays()) {
            System.out.printf("- %s: %s, %s\n", weatherForecastDay.getLocalDate(), weatherForecastDay.getCondition(), weatherForecastDay.getTemperature());
        }
    }
}