package org.example.auth;

public record TokenResponse(String token, String username, String Role) {
}
