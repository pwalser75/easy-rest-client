package ch.frostnova.web.eastrestclient.http;

/**
 * A bound {@code @FormParam} argument: the field name and the actual value.
 */
final class FormField {

    private final String name;
    private final Object value;

    FormField(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    String getName() {
        return name;
    }

    Object getValue() {
        return value;
    }
}
