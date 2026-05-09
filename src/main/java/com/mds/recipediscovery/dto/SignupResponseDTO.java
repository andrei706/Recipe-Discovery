package com.mds.recipediscovery.dto;

public class SignupResponseDTO {

    private final String token;
    private final String tokenType;
    private final long expiresInMs;
    private final Integer userId;
    private final String username;
    private final String email;

    public SignupResponseDTO(String token, String tokenType, long expiresInMs, Integer userId, String username, String email) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresInMs = expiresInMs;
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
