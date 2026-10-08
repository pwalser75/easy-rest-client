package ch.frostnova.web.eastrestclient.files.backend;

import ch.frostnova.web.eastrestclient.TestFiles;
import ch.frostnova.web.eastrestclient.files.api.FileInfo;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@RequestMapping(path = "api/files")
public class FileController {

    @GetMapping(path = "{name}", produces = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable("name") String name) {
        if ("missing".equals(name)) {
            throw new NoSuchElementException("no such file: " + name);
        }
        var content = TestFiles.content(name);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @PostMapping(path = "upload", consumes = APPLICATION_OCTET_STREAM_VALUE, produces = APPLICATION_JSON_VALUE)
    public FileInfo upload(@RequestHeader("X-Filename") String filename, @RequestBody byte[] content) {
        return new FileInfo(filename, content.length, TestFiles.sha256(content));
    }
}
