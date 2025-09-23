package com.ehb.connected.exceptions.handling;

import com.ehb.connected.exceptions.BaseRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(value = BaseRuntimeException.class)
    protected ResponseEntity<Object> handleBaseRuntimeException(BaseRuntimeException exception, HttpServletRequest request) {
        return logAndReturnError(exception, exception.getStatus(), request);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ExceptionResponse> handleAllExceptions(Exception ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<ExceptionResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
        logger.error(String.format("[ERROR] %s %s %s", request.getMethod(), request.getServletPath(), ex.getMessage()));
        return new ResponseEntity<>(exceptionResponse, HttpStatus.FORBIDDEN);
    }

    @Override
    protected @NotNull ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String key = "error";
            if (error instanceof FieldError) {
                key = ((FieldError) error).getField();
            }
            final String errorMessage = error.getDefaultMessage();
            errors.put(key, errorMessage);
        });
        final HttpServletRequest httpServletRequest = ((ServletWebRequest) request).getRequest();
        logger.error(String.format("[ERROR] %s: %s with message: %s", httpServletRequest.getMethod(), httpServletRequest.getServletPath(), errors));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> logAndReturnError(Throwable throwable, HttpStatus httpStatus, HttpServletRequest request) {
        logger.error(String.format("[ERROR] %s %s - with message: %s", request.getMethod(), request.getServletPath(), throwable.getMessage()));
        throwable.printStackTrace();
        return new ResponseEntity<>(new ExceptionResponse(throwable.getMessage(), httpStatus), httpStatus);
    }
}
