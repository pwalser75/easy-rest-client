package ch.frostnova.web.eastrestclient.http;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class MediaTypesTest {

    @Test
    void shouldDetermineBaseType() {
        assertThat(MediaTypes.baseType(null)).isNull();
        assertThat(MediaTypes.baseType("application/json")).isEqualTo("application/json");
        assertThat(MediaTypes.baseType("application/json; charset=UTF-8")).isEqualTo("application/json");
        assertThat(MediaTypes.baseType("APPLICATION/JSON; charset=UTF-8")).isEqualTo("application/json");
        assertThat(MediaTypes.baseType(" text/plain ; charset=utf-8")).isEqualTo("text/plain");
    }

    @Test
    void shouldMatchMediaTypes() {
        assertThat(MediaTypes.matches("application/json", "application/json")).isTrue();
        assertThat(MediaTypes.matches("application/json; charset=UTF-8", "application/json")).isTrue();
        assertThat(MediaTypes.matches("application/problem+json", "application/json")).isTrue();
        assertThat(MediaTypes.matches("application/xml", "application/json")).isFalse();

        assertThat(MediaTypes.matches("application/json", "*/*")).isTrue();
        assertThat(MediaTypes.matches("application/json", "application/*")).isTrue();
        assertThat(MediaTypes.matches("text/plain", "text/*")).isTrue();
        assertThat(MediaTypes.matches("application/json", "text/*")).isFalse();

        assertThat(MediaTypes.matches(null, "application/json")).isFalse();
        assertThat(MediaTypes.matches("application/json", null)).isFalse();
        assertThat(MediaTypes.matches("application/json", "application")).isFalse();
        assertThat(MediaTypes.matches("application", "application/json")).isFalse();
    }

    @Test
    void shouldDetermineCharset() {
        assertThat(MediaTypes.charset("application/json; charset=UTF-16", UTF_8)).isEqualTo(UTF_16);
        assertThat(MediaTypes.charset("application/json; charset=\"UTF-8\"", UTF_16)).isEqualTo(UTF_8);
        assertThat(MediaTypes.charset("application/json", UTF_8)).isEqualTo(UTF_8);
        assertThat(MediaTypes.charset(null, UTF_8)).isEqualTo(UTF_8);
        assertThat(MediaTypes.charset("application/json; charset=bogus", UTF_8)).isEqualTo(UTF_8);
        assertThat(MediaTypes.charset(null, null)).isEqualTo(UTF_8);
    }
}
