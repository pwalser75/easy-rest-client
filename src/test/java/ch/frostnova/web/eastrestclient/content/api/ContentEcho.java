package ch.frostnova.web.eastrestclient.content.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "content-echo")
@JsonPropertyOrder({"name", "value", "tag"})
public class ContentEcho {

    @JacksonXmlProperty(localName = "name")
    private String name;

    @JacksonXmlProperty(localName = "value")
    private String value;

    @JacksonXmlElementWrapper(localName = "tag", useWrapping = false)
    private List<String> tags = new ArrayList<>();

    public ContentEcho() {
    }

    public ContentEcho(String name, String value, List<String> tags) {
        this.name = name;
        this.value = value;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentEcho)) return false;
        ContentEcho that = (ContentEcho) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value) && Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, tags);
    }

    @Override
    public String toString() {
        return "ContentEcho{name='" + name + "', value='" + value + "', tags=" + tags + '}';
    }
}
