package ch.frostnova.web.eastrestclient.multipart;

import ch.frostnova.web.eastrestclient.RestClient;
import ch.frostnova.web.eastrestclient.TestFiles;
import ch.frostnova.web.eastrestclient.multipart.api.MetaInfo;
import ch.frostnova.web.eastrestclient.multipart.api.MultipartClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.http.HttpClient;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Multipart form data test.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MultipartClientTest {

    @LocalServerPort
    private int port;

    private MultipartClient multipartClient;

    @BeforeEach
    void init() {
        var baseUrl = String.format("http://localhost:%d/", port);
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();
        multipartClient = RestClient.build(httpClient, baseUrl, MultipartClient.class);
    }

    @Test
    void shouldUploadMultipartForm() throws Exception {
        var content = TestFiles.content("multipart-upload");
        var file = Files.createTempFile("easy-rest-client-upload-", ".bin");
        try {
            Files.write(file, content);

            var result = multipartClient.upload("Hello multipart", file, List.of("one", "two", "three"));

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Hello multipart");
            assertThat(result.getFilename()).isEqualTo(file.getFileName().toString());
            assertThat(result.getFileSize()).isEqualTo(content.length);
            assertThat(result.getFileSha256()).isEqualTo(TestFiles.sha256(content));
            assertThat(result.getTags()).containsExactly("one", "two", "three");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void shouldUploadMultipartWithStructuredPart() throws Exception {
        var content = TestFiles.content("multipart-meta");
        var file = Files.createTempFile("easy-rest-client-upload-", ".bin");
        try {
            Files.write(file, content);

            var result = multipartClient.uploadWithMeta("With metadata", file, new MetaInfo("Alice", 3));

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("With metadata");
            assertThat(result.getFilename()).isEqualTo(file.getFileName().toString());
            assertThat(result.getFileSize()).isEqualTo(content.length);
            assertThat(result.getFileSha256()).isEqualTo(TestFiles.sha256(content));
            assertThat(result.getMeta()).contains("\"author\":\"Alice\"").contains("\"revision\":3");
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
