package com.ehb.connected.exceptions;

import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DeadlineExpiredException extends BaseRuntimeException {
        private final HttpStatus status = HttpStatus.GONE;

        public DeadlineExpiredException(DeadlineRestriction restriction) {
                super("Deadline for " + restriction + " has passed.", HttpStatus.GONE);
        }
}
