package com.bookmarks;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class BookmarkResponse {
    private Long id;
    private Long userId;
    private String url;
    private String title;
    private String notes;
    private String createdAt;
    private List<TagResponse> tags;

    public BookmarkResponse() {}

    public BookmarkResponse(Bookmark bookmark) {
        this.id = bookmark.getId();
        this.userId = bookmark.getUser() != null ? bookmark.getUser().getId() : null;
        this.url = bookmark.getUrl();
        this.title = bookmark.getTitle();
        this.notes = bookmark.getNotes();
        this.createdAt = bookmark.getCreatedAt() != null ? bookmark.getCreatedAt().toString() : null;
        this.tags = bookmark.getTags() != null ? bookmark.getTags().stream()
            .map(tag -> new TagResponse(tag.getId(), tag.getName(), tag.getCreatedAt()))
            .collect(Collectors.toList()) : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public List<TagResponse> getTags() { return tags; }
    public void setTags(List<TagResponse> tags) { this.tags = tags; }
}
