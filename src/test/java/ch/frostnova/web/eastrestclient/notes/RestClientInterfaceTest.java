package ch.frostnova.web.eastrestclient.notes;

import ch.frostnova.web.eastrestclient.http.RestClientInterface;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class RestClientInterfaceTest {
    
    @Test
    void shouldScanAndBindMethods() {

        RestClientInterface<NotesClient> restClientInterface = new RestClientInterface<>(NotesClient.class);
        for (Method method : NotesClient.class.getDeclaredMethods()) {
            assertThatCode(() -> restClientInterface.get(method)).doesNotThrowAnyException();
            assertThat(restClientInterface.get(method)).isNotNull();
        }
    }
}
