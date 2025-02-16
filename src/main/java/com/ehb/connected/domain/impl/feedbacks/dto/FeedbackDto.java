package com.ehb.connected.domain.impl.feedbacks.dto;

import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FeedbackDto {

    private Long id;
    private String comment;
    private UserDetailsDto user;
    private ProjectDetailsDto project;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
