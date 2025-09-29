package com.ehb.connected.domain.impl.bugs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BugCreateDto {
    @NotBlank
    @Size(max=8000)
    private String description;
    private String route;
    private String appVersion;
}
