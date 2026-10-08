package ch.frostnova.web.eastrestclient.files;

import ch.frostnova.web.eastrestclient.RestClient;
import ch.frostnova.web.eastrestclient.TestFiles;
import ch.frostnova.web.eastrestclient.files.api.FileClient;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Binary file download and upload test.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileClientTest {

    @LocalServerPort
    private int port;

    private FileClient fileClient;

    @BeforeEach
    void init() {
        var baseUrl = String.format("http://localhost:%d/", port);
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();
        fileClient = RestClient.build(httpClient, baseUrl, FileClient.class);
    }

    @Test
    void shouldDownloadToOutputStream() throws Exception {
        var expected = TestFiles.content("report.txt");
        var target = Files.createTempFile("easy-rest-client-download-", ".bin");
        try {
            try (var out = Files.newOutputStream(target)) {
                fileClient.download("report.txt", out);
            }
            assertThat(Files.readAllBytes(target)).isEqualTo(expected);
        } finally {
            Files.deleteIfExists(target);
        }
    }

    @Test
    void shouldDownloadToPath() throws Exception {
        var expected = TestFiles.content("report.txt");
        var target = fileClient.downloadToPath("report.txt");
        try {
            assertThat(target).exists();
            assertThat(Files.readAllBytes(target)).isEqualTo(expected);
        } finally {
            Files.deleteIfExists(target);
        }
    }

    @Test
    void shouldDownloadToFileSink() throws Exception {
        var expected = TestFiles.content("report.txt");
        var target = Files.createTempFile("easy-rest-client-download-", ".bin").toFile();
        try {
            fileClient.downloadToFile("report.txt", target);
            assertThat(Files.readAllBytes(target.toPath())).isEqualTo(expected);
        } finally {
            Files.deleteIfExists(target.toPath());
        }
    }

    @Test
    void shouldDownloadToTempFile() throws Exception {
        var expected = TestFiles.content("report.txt");
        var target = fileClient.downloadToFile("report.txt");
        try {
            assertThat(target).exists();
            assertThat(Files.readAllBytes(target.toPath())).isEqualTo(expected);
        } finally {
            Files.deleteIfExists(target.toPath());
        }
    }

    @Test
    void shouldNotWriteErrorResponseToOutputSink() throws Exception {
        var target = Files.createTempFile("easy-rest-client-error-", ".bin");
        try (var out = Files.newOutputStream(target)) {
            assertThatThrownBy(() -> fileClient.download("missing", out))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("no such file");
        } finally {
            assertThat(Files.size(target)).isZero();
            Files.deleteIfExists(target);
        }
    }

    @Test
    void shouldUploadFromPath() throws Exception {
        var content = TestFiles.content("upload.bin");
        var file = Files.createTempFile("easy-rest-client-upload-", ".bin");
        try {
            Files.write(file, content);
            var info = fileClient.upload("upload.bin", file);

            assertThat(info.getFilename()).isEqualTo("upload.bin");
            assertThat(info.getSize()).isEqualTo(content.length);
            assertThat(info.getSha256()).isEqualTo(TestFiles.sha256(content));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void shouldUploadFromBytes() {
        var content = TestFiles.content("bytes.bin");
        var info = fileClient.uploadBytes("bytes.bin", content);

        assertThat(info.getFilename()).isEqualTo("bytes.bin");
        assertThat(info.getSize()).isEqualTo(content.length);
        assertThat(info.getSha256()).isEqualTo(TestFiles.sha256(content));
    }

    @Test
    void shouldUploadFromByteBuffer() {
        var content = TestFiles.content("buffer.bin");
        var info = fileClient.uploadBuffer("buffer.bin", ByteBuffer.wrap(content));

        assertThat(info.getFilename()).isEqualTo("buffer.bin");
        assertThat(info.getSize()).isEqualTo(content.length);
        assertThat(info.getSha256()).isEqualTo(TestFiles.sha256(content));
    }

    @Test
    void shouldUploadFromInputStream() {
        var content = TestFiles.content("input-stream.bin");
        var info = fileClient.uploadInputStream("input-stream.bin", new ByteArrayInputStream(content));

        assertThat(info.getFilename()).isEqualTo("input-stream.bin");
        assertThat(info.getSize()).isEqualTo(content.length);
        assertThat(info.getSha256()).isEqualTo(TestFiles.sha256(content));
    }

    @Test
    void shouldUploadFromBodyPublisher() {
        var content = TestFiles.content("publisher.bin");
        var info = fileClient.uploadPublisher("publisher.bin", HttpRequest.BodyPublishers.ofByteArray(content));

        assertThat(info.getFilename()).isEqualTo("publisher.bin");
        assertThat(info.getSize()).isEqualTo(content.length);
        assertThat(info.getSha256()).isEqualTo(TestFiles.sha256(content));
    }

    @Test
    void shouldUploadFromWriter() {
        var content = TestFiles.content("streamed.bin");
        var info = fileClient.uploadStream("streamed.bin", out -> {
            try {
                out.write(content);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });

        assertThat(info.getFilename()).isEqualTo("streamed.bin");
        assertThat(info.getSize()).isEqualTo(content.length);
        assertThat(info.getSha256()).isEqualTo(TestFiles.sha256(content));
    }
}
