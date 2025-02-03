package com.ehb.connected.domain.impl.users.mappers;

import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsMapper {
    //map a user to a user details dto without a principal
    public UserDetailsDto toDto(User user) {
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        userDetailsDto.setId(user.getId());
        userDetailsDto.setEmail(user.getEmail());
        userDetailsDto.setFirstName(user.getFirstName());
        userDetailsDto.setLastName(user.getLastName());
        userDetailsDto.setAvatarUrl(null);
        userDetailsDto.setRole(user.getRole());
        return userDetailsDto;
    }

    //map a user to a user details dto with a principal
    public  UserDetailsDto toDtoWithPrincipal(User user, OAuth2User principal) {
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        userDetailsDto.setId(user.getId());
        userDetailsDto.setEmail(user.getEmail());
        userDetailsDto.setFirstName(user.getFirstName());
        userDetailsDto.setLastName(user.getLastName());
        userDetailsDto.setAvatarUrl(principal.getAttribute("picture"));
        userDetailsDto.setRole(user.getRole());
        return userDetailsDto;
    }
}
