package com.ehb.connected.domain.impl.users.dto;


import com.ehb.connected.domain.impl.tags.entities.Tag;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDetailsDto {
    private Long id;
    private String email;
    private String profileImageUrl;
    private String firstName;
    private String lastName;
    private String role;
    private String linkedinUrl;
    private String fieldOfStudy;
    private String aboutMe;
    private List<Tag> Tags;


    //TODO: add createdProjects/ projects list ?

}
