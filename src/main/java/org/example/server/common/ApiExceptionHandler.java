package org.example.server.common;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> denied(AccessDeniedException e) {
        return Map.of("message", e.getMessage(), "status", 403, "timestamp", OffsetDateTime.now().toString());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> notFound(IllegalArgumentException e) {
        return Map.of("message", e.getMessage(), "status", 404, "timestamp", OffsetDateTime.now().toString());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> conflict(IllegalStateException e) {
        return Map.of("message", e.getMessage(), "status", 409, "timestamp", OffsetDateTime.now().toString());
    }
}