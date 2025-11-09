package com.ehb.connected.domain.impl.bugs.services;

import com.ehb.connected.domain.impl.bugs.dto.BugCreateDto;
import com.ehb.connected.domain.impl.bugs.dto.BugDetailsDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface BugService {

    void create(Authentication authentication, BugCreateDto bugCreateDto);
    List<BugDetailsDto> getAll();
}
