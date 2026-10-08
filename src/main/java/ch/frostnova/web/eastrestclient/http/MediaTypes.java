package ch.frostnova.web.eastrestclient.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Utilities for parsing and comparing media types (as used in HTTP {@code Content-Type} headers).
 * <p>
 * In contrast to a plain string comparison, this class ignores media type parameters (such as {@code charset}),
 * supports wildcards, and treats structured syntax suffixes (e.g. {@code application/problem+json}) as compatible
 * with their base media type (e.g. {@code application/json}).
 */
public final class MediaTypes {

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String TEXT_PLAIN = "text/plain";

    private MediaTypes() {

    }

    /**
     * Returns the base media type of a content type header value, without parameters (e.g. {@code application/json}
     * for {@code application/json; charset=UTF-8}). Returns {@code null} if the input is {@code null}.
     *
     * @param contentType content type header value (may contain parameters, may be null)
     * @return base media type, in lower case
     */
    public static String baseType(String contentType) {
        if (contentType == null) {
            return null;
        }
        var separator = contentType.indexOf(';');
        var baseType = (separator >= 0 ? contentType.substring(0, separator) : contentType).trim();
        return baseType.toLowerCase(Locale.ROOT);
    }

    /**
     * Checks whether the actual media type is compatible with the expected media type. Parameters are ignored,
     * a subtype wildcard (e.g. {@code application/*}) is supported, and structured syntax suffixes are considered
     * compatible (e.g. {@code application/problem+json} matches {@code application/json}).
     *
     * @param contentType actual media type (may contain parameters, may be null)
     * @param mediaType   expected media type (may be null)
     * @return true if the media types are compatible
     */
    public static boolean matches(String contentType, String mediaType) {
        var actual = baseType(contentType);
        var expected = baseType(mediaType);
        if (actual == null || expected == null) {
            return false;
        }
        if (actual.equals(expected)) {
            return true;
        }
        if ("*/*".equals(expected)) {
            return true;
        }
        var expectedSlash = expected.indexOf('/');
        var actualSlash = actual.indexOf('/');
        if (expectedSlash < 0 || actualSlash < 0) {
            return false;
        }
        var expectedType = expected.substring(0, expectedSlash);
        var expectedSubType = expected.substring(expectedSlash + 1);
        var actualType = actual.substring(0, actualSlash);
        var actualSubType = actual.substring(actualSlash + 1);
        if (actualType.equals(expectedType) && "*".equals(expectedSubType)) {
            return true;
        }
        return actualType.equals(expectedType) && actualSubType.endsWith("+" + expectedSubType);
    }

    /**
     * Determines the charset of a content type header value, defaulting to the given fallback charset when no
     * charset parameter is present.
     *
     * @param contentType  content type header value (may be null)
     * @param fallback     charset to use when none is specified
     * @return the charset to use (never null)
     */
    public static Charset charset(String contentType, Charset fallback) {
        if (contentType != null) {
            for (String part : contentType.split(";")) {
                var trimmed = part.trim();
                if (trimmed.toLowerCase(Locale.ROOT).startsWith("charset=")) {
                    var value = trimmed.substring("charset=".length()).trim().replace("\"", "");
                    try {
                        return Charset.forName(value);
                    } catch (RuntimeException ignored) {
                        // fall through to fallback
                    }
                }
            }
        }
        return fallback != null ? fallback : StandardCharsets.UTF_8;
    }
}
