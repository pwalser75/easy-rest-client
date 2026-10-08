package ch.frostnova.web.eastrestclient.multipart.backend;

import ch.frostnova.web.eastrestclient.TestFiles;
import ch.frostnova.web.eastrestclient.multipart.api.UploadResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping(path = "api/multipart")
public class MultipartController {

    @PostMapping(path = "upload", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    public UploadResult upload(@RequestParam("title") String title,
                               @RequestParam("file") MultipartFile file,
                               @RequestParam(value = "tag", required = false) List<String> tags) throws IOException {
        var content = file.getBytes();
        return new UploadResult(title, file.getOriginalFilename(), file.getSize(),
                TestFiles.sha256(content), tags != null ? tags : List.of());
    }

    @PostMapping(path = "upload-with-meta", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    public UploadResult uploadWithMeta(@RequestParam("title") String title,
                                       @RequestParam("file") MultipartFile file,
                                       @RequestParam("meta") String meta) throws IOException {
        var content = file.getBytes();
        var result = new UploadResult(title, file.getOriginalFilename(), file.getSize(),
                TestFiles.sha256(content), List.of());
        result.setMeta(meta);
        return result;
    }
}
