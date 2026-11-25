package com.hotelcheckin.api;

import com.hotelcheckin.service.ApiException;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
    return response(exception.getStatus(), exception.getCode(), exception.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
    String message =
        exception.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage())
            .distinct()
            .collect(Collectors.joining(" "));
    return response(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        message.isBlank() ? "Please check the submitted details." : message);
  }

  @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
    return response(
        HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Please check the submitted details.");
  }

  @ExceptionHandler({DataAccessException.class, CannotCreateTransactionException.class})
  public ResponseEntity<ErrorResponse> handleDatabaseUnavailable(Exception exception) {
    return response(
        HttpStatus.SERVICE_UNAVAILABLE,
        "SERVICE_UNAVAILABLE",
        "The booking system is temporarily unavailable. Please try again shortly.");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
    if (hasDatabaseFailure(exception)) {
      return response(
          HttpStatus.SERVICE_UNAVAILABLE,
          "SERVICE_UNAVAILABLE",
          "The booking system is temporarily unavailable. Please try again shortly.");
    }
    return response(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "Something went wrong. Please try again or visit the front desk.");
  }

  private boolean hasDatabaseFailure(Throwable exception) {
    Throwable current = exception;
    while (current != null) {
      if (current instanceof SQLException
          || current instanceof SQLTimeoutException
          || current instanceof SQLTransientException) return true;
      current = current.getCause();
    }
    return false;
  }

  private ResponseEntity<ErrorResponse> response(HttpStatus status, String code, String message) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(OffsetDateTime.now(ZoneOffset.UTC), status.value(), code, message));
  }
}
