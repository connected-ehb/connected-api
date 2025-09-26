package com.ehb.connected.exceptions.handling;

import com.ehb.connected.exceptions.BaseRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    // --- Domain / custom runtime exceptions ------------------------------

    @ExceptionHandler(BaseRuntimeException.class)
    public ResponseEntity<ProblemDetail> handleBaseRuntimeException(
            BaseRuntimeException ex, HttpServletRequest request) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        pd.setTitle(ex.getClass().getSimpleName());
        pd.setType(URI.create("about:blank"));
        pd.setProperty("path", request.getRequestURI());

        log.error("[ERROR] {} {} - {}", request.getMethod(), request.getServletPath(), ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatus()).body(pd);
    }

    // --- Security --------------------------------------------------------

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Authentication required.");
        pd.setTitle("Unauthorized");
        pd.setProperty("path", req.getRequestURI());
        log.warn("[401] {} {} - {}", req.getMethod(), req.getServletPath(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access is denied.");
        pd.setTitle("Forbidden");
        pd.setProperty("path", req.getRequestURI());
        log.warn("[403] {} {} - {}", req.getMethod(), req.getServletPath(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);
    }

    // --- Validation (body) ----------------------------------------------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed.");
        pd.setTitle("Bad Request");
        pd.setType(URI.create("about:blank"));

        // Collect field errors
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (a, b) -> a)); // keep first

        // Global/object errors (non-field)
        var globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : err.getObjectName() + " invalid")
                .toList();

        pd.setProperty("fieldErrors", fieldErrors);
        if (!globalErrors.isEmpty()) {
            pd.setProperty("globalErrors", globalErrors);
        }

        var httpReq = ((ServletWebRequest) request).getRequest();
        pd.setProperty("path", httpReq.getRequestURI());

        log.debug("[400] {} {} - Validation errors: fields={}, globals={}",
                httpReq.getMethod(), httpReq.getServletPath(), fieldErrors, globalErrors);

        return ResponseEntity.badRequest().body(pd);
    }

    // --- Validation (params / path variables) ---------------------------

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Constraint violation.");
        pd.setTitle("Bad Request");
        pd.setProperty("path", req.getRequestURI());
        var violations = ex.getConstraintViolations().stream()
                .collect(java.util.stream.Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (a, b) -> a));
        pd.setProperty("violations", violations);
        log.debug("[400] {} {} - Constraint violations: {}", req.getMethod(), req.getServletPath(), violations);
        return ResponseEntity.badRequest().body(pd);
    }

    // --- Malformed JSON / wrong payload type ----------------------------

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        var httpReq = ((ServletWebRequest) request).getRequest();
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed JSON request.");
        pd.setTitle("Bad Request");
        pd.setProperty("path", httpReq.getRequestURI());

        log.debug("[400] {} {} - Malformed JSON: {}", httpReq.getMethod(), httpReq.getServletPath(), ex.getMessage());
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Invalid parameter '%s'".formatted(ex.getName()));
        pd.setTitle("Bad Request");
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("expectedType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        pd.setProperty("value", String.valueOf(ex.getValue()));
        log.debug("[400] {} {} - Type mismatch: {}", req.getMethod(), req.getServletPath(), ex.getMessage());
        return ResponseEntity.badRequest().body(pd);
    }

    // --- Catch-all (don’t leak internals) -------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAllExceptions(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.");
        pd.setTitle("Internal Server Error");
        pd.setProperty("path", req.getRequestURI());

        log.error("[500] {} {} - Unexpected error", req.getMethod(), req.getServletPath(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }
}
