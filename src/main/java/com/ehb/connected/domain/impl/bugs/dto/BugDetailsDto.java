package com.ehb.connected.domain.impl.bugs.dto;

import com.ehb.connected.domain.impl.users.entities.User;
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
    private User createdBy;
    private LocalDateTime createdAt;
}
