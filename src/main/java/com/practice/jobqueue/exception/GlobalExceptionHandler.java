package com.practice.jobqueue.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralizes exception handling for the application's REST API.
 * <p>
 * This advice maps common framework and domain exceptions to consistent HTTP
 * responses so clients receive predictable error payloads.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles request validation failures raised during data binding.
     *
     * @param ex the validation exception containing field errors
     * @return a {@code 400 Bad Request} response with messages keyed by field name
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles invalid arguments detected by application logic.
     *
     * @param ex the exception describing the invalid argument
     * @return a {@code 400 Bad Request} response containing the error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {

        return ResponseEntity.badRequest()
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles lookups for entities that cannot be found.
     *
     * @param ex the exception describing the missing entity
     * @return a {@code 404 Not Found} response containing the error message
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handles unexpected exceptions that are not covered by more specific handlers.
     *
     * @param ex the unexpected exception
     * @return a {@code 500 Internal Server Error} response with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
    }
}
