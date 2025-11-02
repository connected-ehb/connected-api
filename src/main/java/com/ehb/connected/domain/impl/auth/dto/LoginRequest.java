package com.ehb.connected.domain.impl.auth.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for form-based login (researchers without Canvas account).
 */
@Getter
@Setter
public class LoginRequest {
    private String email;
    private String password;
}
