package ch.frostnova.web.eastrestclient.http;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class FormEncoderTest {

    @Test
    void shouldEncodeFormFields() {
        var fields = new LinkedHashMap<String, List<String>>();
        fields.put("name", List.of("Ä ö"));
        fields.put("tag", List.of("a", "b"));

        assertThat(FormEncoder.encode(fields, UTF_8)).isEqualTo("name=%C3%84+%C3%B6&tag=a&tag=b");
    }

    @Test
    void shouldEncodeNullValueAsEmpty() {
        var fields = new LinkedHashMap<String, List<String>>();
        fields.put("empty", java.util.Collections.singletonList(null));

        assertThat(FormEncoder.encode(fields, UTF_8)).isEqualTo("empty=");
    }
}
