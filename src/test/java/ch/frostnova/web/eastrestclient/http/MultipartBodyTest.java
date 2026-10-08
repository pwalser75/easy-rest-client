package ch.frostnova.web.eastrestclient.http;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultipartBodyTest {

    @Test
    void shouldEncodeMultipartBody() throws Exception {
        var body = new MultipartBody();
        body.addText("title", "Hello", null);
        body.addBytes("data", "data.bin", "application/octet-stream", new byte[]{1, 2, 3});

        byte[] bytes;
        try (var in = body.openStream()) {
            bytes = in.readAllBytes();
        }
        var content = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        var boundary = body.getBoundary();

        assertThat(content)
                .startsWith("--" + boundary + "\r\n")
                .contains("Content-Disposition: form-data; name=\"title\"")
                .contains("Hello")
                .contains("Content-Disposition: form-data; name=\"data\"; filename=\"data.bin\"")
                .contains("Content-Type: application/octet-stream")
                .contains("\u0001\u0002\u0003")
                .endsWith("--" + boundary + "--\r\n");
    }

    @Test
    void shouldEscapePartNamesAndFileNames() throws Exception {
        var body = new MultipartBody();
        body.addText("we\"ird", "value", null);
        body.addBytes("file", "a\"b\r\nc.bin", "application/octet-stream", new byte[0]);

        String content;
        try (var in = body.openStream()) {
            content = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.ISO_8859_1);
        }

        assertThat(content)
                .contains("name=\"we\\\"ird\"")
                .contains("filename=\"a\\\"bc.bin\"");
    }
}
