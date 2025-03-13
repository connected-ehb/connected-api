package com.ehb.connected.domain.impl.auth.entities;

import lombok.Getter;

@Getter
public class RegistrationRequestDto {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String invitationCode;
}
