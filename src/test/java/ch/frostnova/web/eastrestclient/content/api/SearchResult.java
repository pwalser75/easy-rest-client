package ch.frostnova.web.eastrestclient.content.api;

import java.util.List;
import java.util.Objects;

public class SearchResult {

    private String query;
    private List<String> tags;
    private List<String> headerTags;

    public SearchResult() {
    }

    public SearchResult(String query, List<String> tags, List<String> headerTags) {
        this.query = query;
        this.tags = tags;
        this.headerTags = headerTags;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getHeaderTags() {
        return headerTags;
    }

    public void setHeaderTags(List<String> headerTags) {
        this.headerTags = headerTags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchResult)) return false;
        SearchResult that = (SearchResult) o;
        return Objects.equals(query, that.query) && Objects.equals(tags, that.tags) && Objects.equals(headerTags, that.headerTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, tags, headerTags);
    }

    @Override
    public String toString() {
        return "SearchResult{query='" + query + "', tags=" + tags + ", headerTags=" + headerTags + '}';
    }
}
