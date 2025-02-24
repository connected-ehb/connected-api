package com.ehb.connected.exceptions;

import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import org.springframework.http.HttpStatus;

public class AccessTokenExpiredException extends BaseRuntimeException{

    public AccessTokenExpiredException() {
        super("Access token has expired.", HttpStatus.UNAUTHORIZED);
    }

}
