package ch.frostnova.web.eastrestclient.http;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Describes where a (potentially large) response body should be streamed to, instead of being buffered in memory.
 * Supported sinks are {@link Path}, {@link File}, and {@link OutputStream}.
 */
final class ResponseSink {

    enum Kind {
        NONE,
        PATH,
        FILE,
        OUTPUT_STREAM
    }

    private static final ResponseSink NONE = new ResponseSink(Kind.NONE, null, null, null);

    private final Kind kind;
    private final Path path;
    private final File file;
    private final OutputStream outputStream;

    private ResponseSink(Kind kind, Path path, File file, OutputStream outputStream) {
        this.kind = kind;
        this.path = path;
        this.file = file;
        this.outputStream = outputStream;
    }

    static ResponseSink none() {
        return NONE;
    }

    static ResponseSink toPath(Path path) {
        return new ResponseSink(Kind.PATH, path, null, null);
    }

    static ResponseSink toFile(File file) {
        return new ResponseSink(Kind.FILE, null, file, null);
    }

    static ResponseSink toOutputStream(OutputStream outputStream) {
        return new ResponseSink(Kind.OUTPUT_STREAM, null, null, outputStream);
    }

    Kind getKind() {
        return kind;
    }

    boolean isPresent() {
        return kind != Kind.NONE;
    }

    Path getPath() {
        return path;
    }

    File getFile() {
        return file;
    }

    OutputStream getOutputStream() {
        return outputStream;
    }
}
