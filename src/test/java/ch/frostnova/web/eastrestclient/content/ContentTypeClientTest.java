package ch.frostnova.web.eastrestclient.content;

import ch.frostnova.web.eastrestclient.RestClient;
import ch.frostnova.web.eastrestclient.content.api.ClassLevelContentClient;
import ch.frostnova.web.eastrestclient.content.api.ContentClient;
import ch.frostnova.web.eastrestclient.content.api.ContentEcho;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.ServiceUnavailableException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Content type (JSON, XML, text, form) and parameter binding test, including class-level annotations and error
 * handling.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContentTypeClientTest {

    @LocalServerPort
    private int port;

    private ContentClient contentClient;
    private ClassLevelContentClient classLevelClient;

    @BeforeEach
    void init() {
        var baseUrl = String.format("http://localhost:%d/", port);
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();
        contentClient = RestClient.build(httpClient, baseUrl, ContentClient.class);
        classLevelClient = RestClient.build(httpClient, baseUrl, ClassLevelContentClient.class);
    }

    @Test
    void shouldExchangeJson() {
        var value = new ContentEcho("greeting", "Aloha", List.of("a", "b"));

        assertThat(contentClient.echoJson(value)).isEqualTo(value);
    }

    @Test
    void shouldExchangeXml() {
        var value = new ContentEcho("greeting", "Aloha", List.of("a", "b"));

        assertThat(contentClient.echoXml(value)).isEqualTo(value);
    }

    @Test
    void shouldExchangeStreamedContentAsText() {
        assertThat(contentClient.echoText("Grüezi mitenand")).isEqualTo("Grüezi mitenand");
    }

    @Test
    void shouldExchangeJsonWithoutExplicitConsumes() {
        var value = new ContentEcho("default", "value", List.of());

        assertThat(contentClient.echoDefault(value)).isEqualTo(value);
    }

    @Test
    void shouldReadTextResponseAndDecodePathParameter() {
        assertThat(contentClient.greeting("Grüezi mitenand")).isEqualTo("Hello Grüezi mitenand");
    }

    @Test
    void shouldBindQueryAndHeaderCollections() {
        var result = contentClient.search("hello world", List.of("a", "b"), List.of("x", "y"));

        assertThat(result.getQuery()).isEqualTo("hello world");
        assertThat(result.getTags()).containsExactly("a", "b");
        assertThat(result.getHeaderTags()).containsExactly("x", "y");
    }

    @Test
    void shouldUseClassLevelAnnotations() {
        var value = new ContentEcho("class", "level", List.of("t"));

        assertThat(classLevelClient.echo(value)).isEqualTo(value);
    }

    @Test
    void shouldThrowForbiddenException() {
        assertThatThrownBy(() -> contentClient.forbidden()).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowNotAcceptableException() {
        assertThatThrownBy(() -> contentClient.notAcceptable()).isInstanceOf(NotAcceptableException.class);
    }

    @Test
    void shouldThrowNotSupportedException() {
        assertThatThrownBy(() -> contentClient.unsupportedMedia(new ContentEcho("x", "y", List.of())))
                .isInstanceOf(NotSupportedException.class);
    }

    @Test
    void shouldThrowClientErrorExceptionForUnknownClientError() {
        assertThatThrownBy(() -> contentClient.teapot()).isExactlyInstanceOf(ClientErrorException.class);
    }

    @Test
    void shouldThrowInternalServerErrorException() {
        assertThatThrownBy(() -> contentClient.serverError()).isInstanceOf(InternalServerErrorException.class);
    }

    @Test
    void shouldThrowServerErrorExceptionForUnknownServerError() {
        assertThatThrownBy(() -> contentClient.badGateway()).isExactlyInstanceOf(ServerErrorException.class);
    }

    @Test
    void shouldThrowServiceUnavailableException() {
        assertThatThrownBy(() -> contentClient.unavailable()).isInstanceOf(ServiceUnavailableException.class);
    }
}
