package ch.frostnova.web.eastrestclient.http;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests that invalid client interfaces are rejected when they are scanned.
 */
class RestMethodAdapterTest {

    @Test
    void shouldRejectMethodWithoutRequestMethod() {
        assertThatThrownBy(() -> new RestClientInterface<>(NoRequestMethod.class))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("no request method annotation found");
    }

    @Test
    void shouldRejectMethodWithMultipleRequestMethods() {
        assertThatThrownBy(() -> new RestClientInterface<>(MultipleRequestMethods.class))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("multiple request method annotations found");
    }

    @Test
    void shouldRejectArgumentWithMultipleParamAnnotations() {
        assertThatThrownBy(() -> new RestClientInterface<>(MultipleParamAnnotations.class))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("more than one param annotation on argument");
    }

    @Test
    void shouldRejectMultipleBodyArguments() {
        assertThatThrownBy(() -> new RestClientInterface<>(MultipleBodies.class))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("more than one body argument");
    }

    @Test
    void shouldRejectFormParamCombinedWithBody() {
        assertThatThrownBy(() -> new RestClientInterface<>(FormAndBody.class))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("cannot combine @FormParam arguments with a body argument");
    }

    @Test
    void shouldRejectMultipleResponseSinks() {
        assertThatThrownBy(() -> new RestClientInterface<>(MultipleSinks.class))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("more than one response sink argument");
    }

    interface NoRequestMethod {

        @Path("x")
        String invalid();
    }

    interface MultipleRequestMethods {

        @GET
        @POST
        String invalid();
    }

    interface MultipleParamAnnotations {

        @GET
        String invalid(@PathParam("a") @QueryParam("a") String value);
    }

    interface MultipleBodies {

        @POST
        void invalid(String first, String second);
    }

    interface FormAndBody {

        @POST
        void invalid(@FormParam("a") String first, String second);
    }

    interface MultipleSinks {

        @GET
        void invalid(OutputStream first, OutputStream second);
    }
}
