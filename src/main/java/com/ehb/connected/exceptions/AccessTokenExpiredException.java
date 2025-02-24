package com.ehb.connected.exceptions;

import org.springframework.http.HttpStatus;

public class AccessTokenExpiredException extends BaseRuntimeException{

    public AccessTokenExpiredException() {
        super("Access token has expired.", HttpStatus.UNAUTHORIZED);
    }

}
