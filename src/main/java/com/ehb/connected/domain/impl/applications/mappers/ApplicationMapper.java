package com.ehb.connected.domain.impl.applications.mappers;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationMapper {

    private final UserDetailsMapper userDetailsMapper;

    public ApplicationDto toDto(Application application) {
        return new ApplicationDto(
                application.getId(),
                application.getMotivationMd(),
                application.getStatus(),
                application.getProject().getId(),
                userDetailsMapper.toUserDetailsDto(application.getApplicant())
        );
    }
}
