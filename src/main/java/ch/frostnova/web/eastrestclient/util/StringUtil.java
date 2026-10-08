package ch.frostnova.web.eastrestclient.util;

import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class StringUtil {

    private StringUtil() {

    }

    public static String urlEncode(Object value) {
        if (value == null) {
            return null;
        }
        return URLEncoder.encode(String.valueOf(value), UTF_8);
    }

    /**
     * Encodes a value for use as a path segment: reserved characters are percent-encoded, and spaces are encoded as
     * {@code %20} (in contrast to form/query encoding, which uses {@code +}).
     *
     * @param value value to encode (may be null)
     * @return the encoded value, or null if the input was null
     */
    public static String pathEncode(Object value) {
        if (value == null) {
            return null;
        }
        return URLEncoder.encode(String.valueOf(value), UTF_8).replace("+", "%20");
    }

    public static String removeLeadingAndTrailingSlashes(String s) {
        if (s == null) {
            return null;
        }
        var stringBuilder = new StringBuilder(s);
        while (stringBuilder.length() > 0 && stringBuilder.charAt(0) == '/') {
            stringBuilder.deleteCharAt(0);
        }
        while (stringBuilder.length() > 0 && stringBuilder.charAt(stringBuilder.length() - 1) == '/') {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }
}
