package com.ehb.connected.domain.impl.users.dto;

import com.ehb.connected.domain.impl.users.entities.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class authUserDetailsDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private Role role;
    private Boolean isVerified;
}
