package com.ehb.connected.domain.impl.users.mappers;

import com.ehb.connected.domain.impl.tags.dto.TagDto;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsMapperTest {

    private final TagMapper tagMapper = new TagMapper();
    private final UserDetailsMapper mapper = new UserDetailsMapper(tagMapper);

    @Test
    void toUserDetailsDtoCopiesAllFields() {
        Tag tag = new Tag();
        tag.setId(7L);
        tag.setName("Spring");

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setProfileImageUrl("https://img");
        user.setRole(Role.TEACHER);
        user.setLinkedinUrl("https://linkedin");
        user.setFieldOfStudy("Applied Informatics");
        user.setAboutMe("About me");
        user.setTags(List.of(tag));
        user.setEmailVerified(true);

        UserDetailsDto dto = mapper.toUserDetailsDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEmail()).isEqualTo("user@example.com");
        assertThat(dto.getFirstName()).isEqualTo("Jane");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getProfileImageUrl()).isEqualTo("https://img");
        assertThat(dto.getRole()).isEqualTo("TEACHER");
        assertThat(dto.getLinkedinUrl()).isEqualTo("https://linkedin");
        assertThat(dto.getFieldOfStudy()).isEqualTo("Applied Informatics");
        assertThat(dto.getAboutMe()).isEqualTo("About me");
        assertThat(dto.getTags()).singleElement()
                .satisfies(tagDto -> {
                    assertThat(tagDto.getId()).isEqualTo(7L);
                    assertThat(tagDto.getName()).isEqualTo("Spring");
                });
        assertThat(dto.getIsVerified()).isTrue();
    }

    @Test
    void toDtoWithoutPrincipalOmitsProfileImage() {
        User user = new User();
        user.setId(2L);
        user.setEmail("user@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setProfileImageUrl("https://img");
        user.setRole(Role.STUDENT);
        user.setEmailVerified(false);

        AuthUserDetailsDto dto = mapper.toDto(user);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getEmail()).isEqualTo("user@example.com");
        assertThat(dto.getProfileImageUrl()).isNull();
        assertThat(dto.getRole()).isEqualTo(Role.STUDENT);
        assertThat(dto.getIsVerified()).isFalse();
    }

    @Test
    void toEntityMapsDtoBackToUser() {
        TagDto tagDto = new TagDto();
        tagDto.setId(11L);
        tagDto.setName("Java");

        UserDetailsDto dto = new UserDetailsDto();
        dto.setId(3L);
        dto.setEmail("user@example.com");
        dto.setFirstName("Alice");
        dto.setLastName("Smith");
        dto.setProfileImageUrl("https://profile");
        dto.setRole("STUDENT");
        dto.setLinkedinUrl("https://linkedin");
        dto.setFieldOfStudy("ICT");
        dto.setAboutMe("About");
        dto.setTags(List.of(tagDto));
        dto.setIsVerified(true);

        User user = mapper.toEntity(dto);

        assertThat(user.getId()).isEqualTo(3L);
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getFirstName()).isEqualTo("Alice");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getProfileImageUrl()).isEqualTo("https://profile");
        assertThat(user.getRole()).isEqualTo(Role.STUDENT);
        assertThat(user.getLinkedinUrl()).isEqualTo("https://linkedin");
        assertThat(user.getFieldOfStudy()).isEqualTo("ICT");
        assertThat(user.getAboutMe()).isEqualTo("About");
        assertThat(user.getTags()).singleElement()
                .satisfies(tag -> {
                    assertThat(tag.getId()).isEqualTo(11L);
                    assertThat(tag.getName()).isEqualTo("Java");
                });
        assertThat(user.isEmailVerified()).isTrue();
    }
}
