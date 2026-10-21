package org.example.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(UsernameAlreadyTakenException.class)
    ResponseEntity<Map<String, Object>> usernameToken(UsernameAlreadyTakenException ex) {
        return errorBody(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validationFailed(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatField)
                .collect(Collectors.joining("; "));
        return errorBody(HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<Map<String, Object>> badCredentials() {
        return errorBody(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    private ResponseEntity<Map<String, Object>> errorBody(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "error", message
        ));
    }

    private String formatField(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }
}
