package ch.frostnova.web.eastrestclient.hello;

import ch.frostnova.web.eastrestclient.RestClient;
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
 * Note endpoint test
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloClientTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeEach
    void init() {
        baseUrl = String.format("http://localhost:%d/", port);
    }

    @Test
    public void shouldSayHello() {
        HelloClient helloClient = RestClient.build(httpClient(), baseUrl, HelloClient.class);

        String messageEn = helloClient.hello("en", "world");
        assertThat(messageEn).isEqualTo("Hello world");

        String messageDe = helloClient.hello("de", "Welt");
        assertThat(messageDe).isEqualTo("Hallo Welt");
    }

    @Test
    public void shouldSayHelloWithDefault() {
        HelloClient helloClient = RestClient.build(httpClient(), baseUrl, HelloClient.class);

        String name = "Frank Drebin";
        String hello = helloClient.hello(name);
        assertThat(hello).endsWith(name);
    }

    @Test
    public void shouldSayHelloWorld() {
        HelloClient helloClient = RestClient.build(httpClient(), baseUrl, HelloClient.class);

        String hello = helloClient.helloWorld();
        assertThat(hello).isEqualTo("Hello World");
    }

    private HttpClient httpClient() {
        return HttpClient.newBuilder()
                //   .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(1))
                .build();
    }
}