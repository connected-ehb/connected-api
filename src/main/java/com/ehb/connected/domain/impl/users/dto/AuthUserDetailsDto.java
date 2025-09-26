package com.ehb.connected.domain.impl.users.dto;

import com.ehb.connected.domain.impl.users.entities.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthUserDetailsDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private Role role;
    private Boolean isVerified;
}
