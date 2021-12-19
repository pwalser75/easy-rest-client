package ch.frostnova.web.eastrestclient.notes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Notes web service endpoint <p>
 * Full local path: <a href="https://localhost/api/notes">https://localhost/api/notes</a>
 */
@RestController
@RequestMapping(path = "api/notes")
@CrossOrigin(origins = "*",
        allowedHeaders = "origin, content-type, accept, authorization",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.HEAD},
        maxAge = 1209600)
public class NotesController {

    @Autowired
    private NoteService noteService;

    /**
     * List notes
     *
     * @return list of notes (never null)
     */
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<Note> list() {
        return noteService.list();
    }

    /**
     * Get a record by id. If the record was not found, a NoSuchElementException will be thrown (resulting in a 404 NOT FOUND).
     *
     * @param id id of the record
     * @return record
     */
    @GetMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Note get(@PathVariable("id") long id) {
        return noteService.get(id);
    }

    /**
     * Create a new record (or updates an existing record, when the id is set).
     *
     * @param note record to create
     * @return created record
     */
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    public Note create(@RequestBody Note note) {
        note.setId(null);
        return noteService.save(note);
    }

    /**
     * Update a record
     *
     * @param id   id of the record to update
     * @param note new data to set
     */
    @PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(NO_CONTENT)
    public void update(@PathVariable("id") long id, @RequestBody Note note) {
        note.setId(id);
        noteService.save(note);
    }

    /**
     * Delete a record
     *
     * @param id id of the record
     */
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable("id") long id) {
        noteService.delete(id);
    }
}