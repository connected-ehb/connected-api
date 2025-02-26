package com.ehb.connected.domain.impl.auth.entities;

import lombok.Getter;

@Getter
public class LoginRequestDto {
    private String email;
    private String password;
}
