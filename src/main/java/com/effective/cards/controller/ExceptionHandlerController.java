package com.effective.cards.controller;

import com.effective.cards.dto.ErrorResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        ErrorResponse errorResponse = switch (status) {
            case NOT_FOUND -> new ErrorResponse(404, "NOT FOUND", ex.getReason());
            case CONFLICT -> new ErrorResponse(409, "CONFLICT", ex.getReason());
            case BAD_REQUEST -> new ErrorResponse(400, "BAD REQUEST", ex.getReason());
            case FORBIDDEN -> new ErrorResponse(403, "FORBIDDEN", ex.getReason());
            case UNAUTHORIZED -> new ErrorResponse(401, "UNAUTHORIZED", ex.getReason());
            default -> new ErrorResponse(status.value(), "UNDEFINED", ex.getReason());
        };
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(400, "BAD REQUEST", ex.getMessage());
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        ErrorResponse errorResponse = new ErrorResponse(400, "BAD REQUEST", ex.getMessage());
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(401, "UNAUTHORIZED", ex.getMessage());
        return ResponseEntity.status(401).body(errorResponse);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(401, "UNAUTHORIZED", ex.getMessage());
        return ResponseEntity.status(401).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException() {
        ErrorResponse errorResponse = new ErrorResponse(
                400, "BAD REQUEST", "Request body validation failed");
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(500, "ITERNAL SERVER ERROR", ex.getMessage());
        return ResponseEntity.status(500).body(errorResponse);
    }
}
