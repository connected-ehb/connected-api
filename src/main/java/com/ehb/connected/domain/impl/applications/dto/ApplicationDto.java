package com.ehb.connected.domain.impl.applications.dto;

import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;
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
    private Project project;
    private UserDetailsDto applicant;
}
