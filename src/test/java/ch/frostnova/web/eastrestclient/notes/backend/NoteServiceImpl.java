package ch.frostnova.web.eastrestclient.notes.backend;

import ch.frostnova.web.eastrestclient.notes.api.Note;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Note service implementation, using an in-memory repository.
 */
@Service
public class NoteServiceImpl implements NoteService {

    private final AtomicInteger sequence = new AtomicInteger(1000);
    private final Map<Long, Note> repository = new TreeMap<>();

    @Override

    public Note get(long id) {
        return Optional.ofNullable(repository.get(id)).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public Note save(Note note) {
        if (note.getId() == null) {
            note.setId((long) sequence.getAndIncrement());
        }
        repository.put(note.getId(), note);
        return note;
    }

    @Override
    public List<Note> list() {
        return repository.values().stream().collect(toUnmodifiableList());
    }

    @Override
    public void delete(long id) {
        repository.remove(id);
    }
}