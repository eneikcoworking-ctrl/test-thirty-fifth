package com.bookmarks;

import java.time.Instant;

public class TagResponse {
    private Long id;
    private String name;
    private String createdAt;

    public TagResponse() {}

    public TagResponse(Long id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt != null ? createdAt.toString() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
