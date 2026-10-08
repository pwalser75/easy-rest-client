package ch.frostnova.web.eastrestclient.forms;

import ch.frostnova.web.eastrestclient.RestClient;
import ch.frostnova.web.eastrestclient.forms.api.FormClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Form parameter test ({@code application/x-www-form-urlencoded}).
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FormClientTest {

    @LocalServerPort
    private int port;

    private FormClient formClient;

    @BeforeEach
    void init() {
        var baseUrl = String.format("http://localhost:%d/", port);
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();
        formClient = RestClient.build(httpClient, baseUrl, FormClient.class);
    }

    @Test
    void shouldSubmitFormParameters() {
        var result = formClient.echo("Äphry von Üetliberg", 42, List.of("alpha", "beta", "gamma"));

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Äphry von Üetliberg");
        assertThat(result.getAge()).isEqualTo(42);
        assertThat(result.getTags()).containsExactly("alpha", "beta", "gamma");
    }

    @Test
    void shouldSubmitFormParametersWithoutRepeatedValues() {
        var result = formClient.echo("Bob", 7, List.of());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Bob");
        assertThat(result.getAge()).isEqualTo(7);
        assertThat(result.getTags()).isEmpty();
    }
}
