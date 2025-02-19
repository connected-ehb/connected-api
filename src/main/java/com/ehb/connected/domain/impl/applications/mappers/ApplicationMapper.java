package com.ehb.connected.domain.impl.applications.mappers;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.projects.mappers.ProjectMapper;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ApplicationMapper {

    private final UserDetailsMapper userDetailsMapper;
    private final ProjectMapper projectMapper;

    public ApplicationDetailsDto toDto(Application application) {
        return new ApplicationDetailsDto(
                application.getId(),
                application.getMotivationMd(),
                application.getStatus(),
                projectMapper.toDetailsDto(application.getProject()),
                userDetailsMapper.toUserDetailsDto(application.getApplicant())
        );
    }
    public List<ApplicationDetailsDto> toDtoList(List<Application> applications) {
        return applications.stream().map(this::toDto).toList();
    }

}
