package ch.frostnova.web.eastrestclient.notes;

import ch.frostnova.web.eastrestclient.http.RestClientInterface;
import ch.frostnova.web.eastrestclient.notes.api.NotesClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class RestClientInterfaceTest {

    @Test
    void shouldScanAndBindMethods() {

        var restClientInterface = new RestClientInterface<NotesClient>(NotesClient.class);
        for (var method : NotesClient.class.getDeclaredMethods()) {
            assertThatCode(() -> restClientInterface.get(method)).doesNotThrowAnyException();
            assertThat(restClientInterface.get(method)).isNotNull();
        }
    }
}
