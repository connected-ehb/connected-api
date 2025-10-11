package com.ehb.connected.domain.impl.projects.dto;

import com.ehb.connected.domain.impl.users.dto.UserSummaryDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectSummaryDto {
    private Long id;
    private String title;
    private String status;
    private UserSummaryDto createdBy;
}
