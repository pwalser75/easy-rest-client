package ch.frostnova.web.eastrestclient.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping(path = "hello")
public class HelloController {

    @GetMapping(path = "/{lang}", produces = TEXT_PLAIN_VALUE)
    public String hello(@PathParam("lang") String lang,
                        @RequestParam("name") String name) {
        if ("de".equals(lang)) {
            return "Hello " + name;
        }
        if ("fr".equals(lang)) {
            return "Salut " + name;
        }
        if ("it".equals(lang)) {
            return "Ciao " + name;
        }
        return "Hello " + name;
    }
}
