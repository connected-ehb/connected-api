package com.ehb.connected.domain.impl.applications.dto;

import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApplicationDto {
    private Long id;
    private String motivationMd;
    private ApplicationStatusEnum status;
    private ProjectDetailsDto project;
    private UserDetailsDto applicant;
}
