package com.bookmarks;

public class TokenResponse {
    private String token;
    private String type;

    public TokenResponse() {}

    public TokenResponse(String token, String type) {
        this.token = token;
        this.type = type;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
