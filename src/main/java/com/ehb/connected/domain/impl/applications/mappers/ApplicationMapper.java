package com.ehb.connected.domain.impl.applications.mappers;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.projects.mappers.ProjectMapper;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationMapper {

    private final UserDetailsMapper userDetailsMapper;
    private final ProjectMapper projectMapper;

    public ApplicationDto toDto(Application application) {
        return new ApplicationDto(
                application.getId(),
                application.getMotivationMd(),
                application.getStatus(),
                projectMapper.toDetailsDto(application.getProject()),
                userDetailsMapper.toUserDetailsDto(application.getApplicant())
        );
    }
}
