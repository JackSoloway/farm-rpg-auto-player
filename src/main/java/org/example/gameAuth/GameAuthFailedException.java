package org.example.gameAuth;

import org.springframework.http.HttpStatus;

public class GameAuthFailedException extends RuntimeException {
    GameAuthFailedException(HttpStatus status) {
        super(status.toString());
    }
}
