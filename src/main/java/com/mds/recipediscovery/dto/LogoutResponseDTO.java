package com.mds.recipediscovery.dto;

public class LogoutResponseDTO {

    private final String message;

    public LogoutResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

