package com.ehb.connected.domain.impl.auth.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for user registration (invite-only researchers).
 */
@Getter
@Setter
public class RegistrationRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String invitationCode;
}
