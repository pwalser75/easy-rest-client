package ch.frostnova.web.eastrestclient.notes;


import ch.frostnova.web.eastrestclient.RestClient;
import ch.frostnova.web.eastrestclient.notes.api.Note;
import ch.frostnova.web.eastrestclient.notes.api.NotesClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.ws.rs.NotFoundException;
import java.net.http.HttpClient;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Note endpoint test
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotesClientTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @LocalServerPort
    private int port;

    private NotesClient notesClient;

    @BeforeEach
    void init() {
        String baseUrl = String.format("http://localhost:%d/", port);
        log.info("BASE URL: " + baseUrl);

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();

        notesClient = RestClient.build(httpClient, baseUrl, NotesClient.class);
    }

    @Test
    public void testCRUD() {

        // create
        Note note = new Note();
        note.setText("Aloha");

        Note created = notesClient.create(note);
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getText()).isEqualTo(note.getText());
        long id = created.getId();
        note = created;

        // read
        Note loaded = notesClient.get(id);
        assertThat(loaded).isNotNull();
        assertThat(loaded.getId()).isNotNull();
        assertThat(loaded.getText()).isEqualTo(note.getText());

        // list
        assertThat(notesClient.list()).isNotEmpty();
        assertThat(notesClient.list()).extracting(Note::getId).contains(id);

        // update
        note.setText("Lorem ipsum dolor sit amet");
        notesClient.update(note.getId(), note);

        loaded = notesClient.get(id);
        assertThat(loaded).isNotNull();
        assertThat(loaded.getId()).isEqualTo(note.getId());
        assertThat(loaded.getText()).isEqualTo(note.getText());

        // delete
        notesClient.delete(id);

        // delete again - must not result in an exception
        notesClient.delete(id);

        // must not be found afterwards
        assertThat(notesClient.list()).extracting(Note::getId).doesNotContain(id);
        assertThatThrownBy(() -> notesClient.get(id)).isInstanceOf(NotFoundException.class);
    }
}