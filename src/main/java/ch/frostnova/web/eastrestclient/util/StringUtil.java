package ch.frostnova.web.eastrestclient.util;

import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public final class StringUtil {

    private StringUtil() {

    }

    public static String urlEncode(Object value) {
        if (value == null) {
            return null;
        }
        return URLEncoder.encode(String.valueOf(value), ISO_8859_1);
    }

    public static String removeLeadingAndTrailingSlashes(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(s);
        while (stringBuilder.length() > 0 && stringBuilder.charAt(0) == '/') {
            stringBuilder.deleteCharAt(0);
        }
        while (stringBuilder.length() > 0 && stringBuilder.charAt(stringBuilder.length() - 1) == '/') {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }
}
