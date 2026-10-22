package com.projet.site.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class SiteExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.NOT_FOUND.value(),
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(DuplicateScheduleExceptionException.class)
    public ResponseEntity<Object> handleDuplicateException(DuplicateScheduleExceptionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.CONFLICT.value(),
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(DuplicateAvailabilityRuleException.class)
    public ResponseEntity<Object> handleDuplicateRule(DuplicateAvailabilityRuleException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.CONFLICT.value(),
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(SlotAlreadyBookedException.class)
    public ResponseEntity<Object> handleSlotAlreadyBooked(SlotAlreadyBookedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.CONFLICT.value(),
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(ContactRateLimitException.class)
    public ResponseEntity<Object> handleContactRateLimit(ContactRateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(ContactUnavailableException.class)
    public ResponseEntity<Object> handleContactUnavailable(ContactUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "message", ex.getMessage()
        ));
    }
}
