package ch.frostnova.web.eastrestclient.content.backend;

import ch.frostnova.web.eastrestclient.content.api.ContentEcho;
import ch.frostnova.web.eastrestclient.content.api.SearchResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.I_AM_A_TEAPOT;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping(path = "api/content")
public class ContentController {

    @PostMapping(path = "json", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ContentEcho json(@RequestBody ContentEcho value) {
        return value;
    }

    @PostMapping(path = "xml", consumes = APPLICATION_XML_VALUE, produces = APPLICATION_XML_VALUE)
    public ContentEcho xml(@RequestBody ContentEcho value) {
        return value;
    }

    @PostMapping(path = "text", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE)
    public String text(@RequestBody String value) {
        return value;
    }

    @PostMapping(path = "default", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ContentEcho defaultJson(@RequestBody ContentEcho value) {
        return value;
    }

    @GetMapping(path = "text/{name}", produces = TEXT_PLAIN_VALUE)
    public String greeting(@PathVariable("name") String name) {
        return "Hello " + name;
    }

    @GetMapping(path = "search", produces = APPLICATION_JSON_VALUE)
    public SearchResult search(@RequestParam(value = "q", required = false) String query,
                               @RequestParam(value = "tag", required = false) List<String> tags,
                               @RequestHeader(value = "X-Tag", required = false) List<String> headerTags) {
        return new SearchResult(query, tags != null ? tags : List.of(), headerTags != null ? headerTags : List.of());
    }

    @GetMapping(path = "forbidden", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> forbidden() {
        return ResponseEntity.status(FORBIDDEN).build();
    }

    @GetMapping(path = "not-acceptable", produces = TEXT_PLAIN_VALUE)
    public String notAcceptable() {
        return "plain";
    }

    @PostMapping(path = "unsupported-media", consumes = TEXT_PLAIN_VALUE, produces = APPLICATION_JSON_VALUE)
    public ContentEcho unsupportedMedia(@RequestBody String value) {
        return new ContentEcho();
    }

    @GetMapping(path = "server-error", produces = APPLICATION_JSON_VALUE)
    public void serverError() {
        throw new IllegalStateException("boom");
    }

    @GetMapping(path = "unavailable", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> unavailable() {
        return ResponseEntity.status(SERVICE_UNAVAILABLE).build();
    }

    @GetMapping(path = "teapot", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> teapot() {
        return ResponseEntity.status(I_AM_A_TEAPOT).build();
    }

    @GetMapping(path = "bad-gateway", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> badGateway() {
        return ResponseEntity.status(BAD_GATEWAY).build();
    }
}
