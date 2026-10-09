package com.qpic.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Every service answers errors with the same {@link ApiError} JSON shape. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiError(Instant.now(), ex.getStatus().value(), ex.getCode(), ex.getMessage(), req.getRequestURI(), null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> fields.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiError(Instant.now(), 400, "VALIDATION_FAILED", "Validation failed", req.getRequestURI(), fields));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest req) {
        if (ex instanceof MaxUploadSizeExceededException) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(new ApiError(Instant.now(), 413, "PAYLOAD_TOO_LARGE", "File is too large", req.getRequestURI(), null));
        }
        log.error("Unhandled error on {}", req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(Instant.now(), 500, "INTERNAL_ERROR", "Unexpected server error", req.getRequestURI(), null));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiError(Instant.now(), 400, "VALIDATION_FAILED", "Validation failed", path(request), fields));
    }

    /** Wraps all remaining Spring MVC exceptions (404, 405, 415, malformed JSON ...) in ApiError. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        HttpStatus resolved = HttpStatus.resolve(statusCode.value());
        String reason = resolved != null ? resolved.getReasonPhrase() : "Error";
        String code = resolved != null ? resolved.name() : "ERROR";
        return ResponseEntity.status(statusCode).headers(headers)
                .body(new ApiError(Instant.now(), statusCode.value(), code, reason, path(request), null));
    }

    private static String path(WebRequest request) {
        return request.getDescription(false).replaceFirst("^uri=", "");
    }
}
