package ch.frostnova.web.eastrestclient.util;

import org.junit.jupiter.api.Test;

import static ch.frostnova.web.eastrestclient.util.StringUtil.removeLeadingAndTrailingSlashes;
import static ch.frostnova.web.eastrestclient.util.StringUtil.urlEncode;
import static org.assertj.core.api.Assertions.assertThat;

public class StringUtilTest {

    @Test
    void shouldRemoveLeadingAndTrailingSlashes() {
        assertThat(removeLeadingAndTrailingSlashes(null)).isNull();
        assertThat(removeLeadingAndTrailingSlashes("")).isEqualTo("");
        assertThat(removeLeadingAndTrailingSlashes("/")).isEqualTo("");
        assertThat(removeLeadingAndTrailingSlashes("//")).isEqualTo("");
        assertThat(removeLeadingAndTrailingSlashes("///")).isEqualTo("");

        assertThat(removeLeadingAndTrailingSlashes("abc")).isEqualTo("abc");
        assertThat(removeLeadingAndTrailingSlashes("abc/def")).isEqualTo("abc/def");
        assertThat(removeLeadingAndTrailingSlashes("abc/def//g")).isEqualTo("abc/def//g");

        assertThat(removeLeadingAndTrailingSlashes("/abc")).isEqualTo("abc");
        assertThat(removeLeadingAndTrailingSlashes("abc/")).isEqualTo("abc");
        assertThat(removeLeadingAndTrailingSlashes("//abc")).isEqualTo("abc");
        assertThat(removeLeadingAndTrailingSlashes("abc//")).isEqualTo("abc");

        assertThat(removeLeadingAndTrailingSlashes("/abc/")).isEqualTo("abc");
        assertThat(removeLeadingAndTrailingSlashes("//abc//")).isEqualTo("abc");
        assertThat(removeLeadingAndTrailingSlashes("///abc///")).isEqualTo("abc");

        assertThat(removeLeadingAndTrailingSlashes("/abc/def/")).isEqualTo("abc/def");
        assertThat(removeLeadingAndTrailingSlashes("//abc//def//")).isEqualTo("abc//def");
    }

    @Test
    void shouldUrlEncode() {
        assertThat(urlEncode(null)).isNull();
        assertThat(urlEncode("HelloWorld")).isEqualTo("HelloWorld");
        assertThat(urlEncode("Hello World")).isEqualTo("Hello+World");
        assertThat(urlEncode("Hello/World")).isEqualTo("Hello%2FWorld");
        assertThat(urlEncode("Hello?Wörld")).isEqualTo("Hello%3FW%F6rld");
    }
}
