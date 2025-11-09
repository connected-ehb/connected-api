package com.ehb.connected.domain.impl.users.dto;

import com.ehb.connected.domain.impl.tags.dto.TagDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MinimalUserDetailsDto {
    private Long id;
    private String profileImageUrl;
    private String firstName;
    private String lastName;
    private String linkedinUrl;
    private String fieldOfStudy;
    private String aboutMe;
    private List<TagDto> tags;
}
