package org.example.gameAuth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GameAuthExceptionHandler {
    @ExceptionHandler(GameAuthFailedException.class)
    ResponseEntity<Map<String, Object>> gameLoginFailed(GameAuthFailedException ex) {
        return errorBody(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> errorBody(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "error", message
        ));
    }
}
