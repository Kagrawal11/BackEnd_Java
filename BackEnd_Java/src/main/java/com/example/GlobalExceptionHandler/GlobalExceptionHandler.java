package com.example.GlobalExceptionHandler;

import com.example.util.ErrorResponse1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Validation Error
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse1> handleValidationErrors(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return new ResponseEntity<>(new ErrorResponse1(msg, 400), HttpStatus.BAD_REQUEST);
    }

    // JWT Authentication Error
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse1> handleBadCredentials(BadCredentialsException ex) {
        return new ResponseEntity<>(new ErrorResponse1("Invalid username or password", 401), HttpStatus.UNAUTHORIZED);
    }

    // OAuth2 Error
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ErrorResponse1> handleOAuth2(OAuth2AuthenticationException ex) {
        return new ResponseEntity<>(new ErrorResponse1("OAuth authentication failed", 401), HttpStatus.UNAUTHORIZED);
    }

    // DB Constraint / SQL Exception
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse1> handleDBErrors(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(new ErrorResponse1("Database error occurred", 500), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse1> handleNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse1(ex.getMessage(), 404), HttpStatus.NOT_FOUND);
    }

    // Explicit status thrown by controllers (e.g. 403 Forbidden for cross-user access) -
    // must be handled before the generic Exception fallback or its real status is lost
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse1> handleResponseStatus(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        String msg = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return new ResponseEntity<>(new ErrorResponse1(msg, code), ex.getStatusCode());
    }

    // Fallback for other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse1> handleAll(Exception ex) {
        logger.error("Unhandled exception", ex);
        return new ResponseEntity<>(new ErrorResponse1("Something went wrong", 500), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

