package ch.frostnova.web.eastrestclient.http;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * Encodes form fields as {@code application/x-www-form-urlencoded} content.
 */
public final class FormEncoder {

    private FormEncoder() {

    }

    /**
     * Encodes the given form fields. Field values may contain multiple values for the same field name.
     *
     * @param fields  ordered form fields (must not be null)
     * @param charset charset to use for encoding (must not be null)
     * @return the URL-encoded form content
     */
    public static String encode(Map<String, List<String>> fields, Charset charset) {
        return fields.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(value -> encode(entry.getKey(), charset) + "=" + encode(value, charset)))
                .collect(joining("&"));
    }

    private static String encode(String value, Charset charset) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, charset);
    }
}
