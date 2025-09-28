package com.ehb.connected.exceptions;

import org.springframework.http.HttpStatus;

public class EmailSendingException extends BaseRuntimeException {
    private final HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;

    public EmailSendingException() {
        super("Error sending email.", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
