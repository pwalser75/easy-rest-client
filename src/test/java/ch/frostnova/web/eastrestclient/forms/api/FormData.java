package ch.frostnova.web.eastrestclient.forms.api;

import java.util.List;
import java.util.Objects;

public class FormData {

    private String name;
    private int age;
    private List<String> tags;

    public FormData() {
    }

    public FormData(String name, int age, List<String> tags) {
        this.name = name;
        this.age = age;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FormData)) {
            return false;
        }
        FormData formData = (FormData) o;
        return age == formData.age && Objects.equals(name, formData.name) && Objects.equals(tags, formData.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, tags);
    }

    @Override
    public String toString() {
        return "FormData{name='" + name + "', age=" + age + ", tags=" + tags + '}';
    }
}
