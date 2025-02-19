package com.ehb.connected.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, code = HttpStatus.FORBIDDEN, reason = "User is not the owner of the project")
public class UserNotOwnerOfProjectException extends RuntimeException {
    public UserNotOwnerOfProjectException() { super(); }
}
