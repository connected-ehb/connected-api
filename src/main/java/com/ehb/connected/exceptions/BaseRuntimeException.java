package com.ehb.connected.exceptions;

import lombok.Getter;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;

@Getter
public class BaseRuntimeException extends RuntimeException {
    private final HttpStatus status;

    public BaseRuntimeException(@NotNull String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
