package ch.frostnova.web.eastrestclient.forms.backend;

import ch.frostnova.web.eastrestclient.forms.api.FormData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "api/forms")
public class FormController {

    @PostMapping(path = "echo", consumes = APPLICATION_FORM_URLENCODED_VALUE, produces = APPLICATION_JSON_VALUE)
    public FormData echo(@RequestParam("name") String name,
                         @RequestParam("age") int age,
                         @RequestParam(value = "tag", required = false) List<String> tags) {
        return new FormData(name, age, tags != null ? tags : List.of());
    }
}
