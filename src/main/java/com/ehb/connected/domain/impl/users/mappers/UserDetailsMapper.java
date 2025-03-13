package com.ehb.connected.domain.impl.users.mappers;

import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.users.dto.authUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsMapper {

    private final TagMapper tagMapper;

    //map a user to a auth user details dto without a principal
    public authUserDetailsDto toDto(User user) {
        authUserDetailsDto userDetailsDto = new authUserDetailsDto();
        userDetailsDto.setId(user.getId());
        userDetailsDto.setEmail(user.getEmail());
        userDetailsDto.setFirstName(user.getFirstName());
        userDetailsDto.setLastName(user.getLastName());
        userDetailsDto.setAvatarUrl(null);
        userDetailsDto.setRole(user.getRole());
        return userDetailsDto;
    }

    //map a user to a auth user details dto with a principal
    public  authUserDetailsDto toDtoWithPrincipal(User user, OAuth2User principal) {
        authUserDetailsDto userDetailsDto = new authUserDetailsDto();
        userDetailsDto.setId(user.getId());
        userDetailsDto.setEmail(user.getEmail());
        userDetailsDto.setFirstName(user.getFirstName());
        userDetailsDto.setLastName(user.getLastName());
        userDetailsDto.setAvatarUrl(null);
        userDetailsDto.setRole(user.getRole());
        return userDetailsDto;
    }

    //map a user to a user details dto
    public UserDetailsDto toUserDetailsDto(User user) {
        UserDetailsDto dto = new UserDetailsDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setRole(user.getRole().name());
        dto.setLinkedinUrl(user.getLinkedinUrl());
        dto.setFieldOfStudy(user.getFieldOfStudy());
        dto.setAboutMe(user.getAboutMe());
        if (user.getTags() != null) {
            dto.setTags(tagMapper.toDtoList(user.getTags()));
        }
        return dto;
    }

    //map a user details dto to a user
    public User toEntity(UserDetailsDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setProfileImageUrl(dto.getProfileImageUrl());
        user.setRole(Role.valueOf(dto.getRole()));
        user.setLinkedinUrl(dto.getLinkedinUrl());
        user.setFieldOfStudy(dto.getFieldOfStudy());
        user.setAboutMe(dto.getAboutMe());
        user.setTags(tagMapper.toEntityList(dto.getTags()));
        return user;
    }
}
