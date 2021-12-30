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

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        String apiKey = UUID.randomUUID().toString();
        WeatherForecast weatherForecast = weatherClient.getForecast(apiKey, "Winterthur");

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

    @Test
    void shouldThrowUnauthorizedExceptionWhenApiKeyIsMissing() {
        assertThatThrownBy(() -> weatherClient.getForecast(null, "Winterthur")).isInstanceOfSatisfying(NotAuthorizedException.class, ex
                -> assertThat(ex.getMessage()).isEqualTo("Access denied, api-key is required"));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenLocationIsMissing() {
        assertThatThrownBy(() -> weatherClient.getForecast(UUID.randomUUID().toString(), null)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowMethodNotAllowedExceptionWhenTryingToDeleteForecast() {
        assertThatThrownBy(() -> weatherClient.deleteForecast()).isInstanceOf(NotAllowedException.class);
    }
}