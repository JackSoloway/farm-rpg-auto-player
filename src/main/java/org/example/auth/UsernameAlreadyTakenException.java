package org.example.auth;

class UsernameAlreadyTakenException extends RuntimeException {
    UsernameAlreadyTakenException(String username) {
        super("Username already taken: " + username);
    }
}
