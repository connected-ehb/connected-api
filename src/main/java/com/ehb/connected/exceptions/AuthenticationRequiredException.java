package com.ehb.connected.exceptions;

import org.springframework.http.HttpStatus;

public class AuthenticationRequiredException extends BaseRuntimeException {
    
    public AuthenticationRequiredException() {
        super("Authentication required", HttpStatus.UNAUTHORIZED);
    }
    
    public AuthenticationRequiredException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
