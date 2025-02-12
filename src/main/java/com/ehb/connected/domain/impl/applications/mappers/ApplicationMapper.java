package com.ehb.connected.domain.impl.applications.mappers;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.applications.entities.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationMapper.class);
    public static ApplicationDto applicationToDto(Application application){
        if (application == null){
            logger.warn("Application is null");
            return null;
        }
        return new ApplicationDto(
                application.getId(),
                application.getMotivationMd(),
                application.getStatus(),
                application.getProject(),
                application.getApplicant()
        );
    }
}
