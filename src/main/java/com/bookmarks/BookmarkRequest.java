package com.bookmarks;

import java.util.List;

public class BookmarkRequest {
    private String url;
    private String title;
    private String notes;
    private List<String> tags;

    public BookmarkRequest() {}

    public BookmarkRequest(String url, String title, String notes, List<String> tags) {
        this.url = url;
        this.title = title;
        this.notes = notes;
        this.tags = tags;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
