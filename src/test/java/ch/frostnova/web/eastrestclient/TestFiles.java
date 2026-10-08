package ch.frostnova.web.eastrestclient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Deterministic test content and checksums, shared between the test web service endpoints and the client tests.
 */
public final class TestFiles {

    private TestFiles() {

    }

    public static byte[] content(String name) {
        var builder = new StringBuilder();
        for (int i = 0; i < 4000; i++) {
            builder.append("line ").append(i).append(" of ").append(name).append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static String sha256(byte[] bytes) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(bytes);
            var builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
