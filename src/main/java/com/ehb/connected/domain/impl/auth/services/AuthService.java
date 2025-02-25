package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.auth.entities.LoginRequestDto;
import com.ehb.connected.domain.impl.auth.entities.RegistrationRequestDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;

import java.security.Principal;

public interface AuthService {
    UserDetailsDto register(RegistrationRequestDto request);
    UserDetailsDto login(LoginRequestDto request);
    void logout(Principal principal);
}
