package com.ehb.connected.domain.impl.bugs.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BugDetailsDto {
    private Long id;
    private String description;
    private String route;
    private String appVersion;
    private String createdBy;
    private LocalDateTime createdAt;
}
