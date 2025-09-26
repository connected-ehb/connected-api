package com.ehb.connected.exceptions;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;

public class UserUnauthorizedException extends BaseRuntimeException {
    public UserUnauthorizedException(@NotNull String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public UserUnauthorizedException(Long userId) {
      super(String.format("User id: %s is not authorized", userId.toString()), HttpStatus.FORBIDDEN);
    }
}
