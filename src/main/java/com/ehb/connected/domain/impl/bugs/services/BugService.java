package com.ehb.connected.domain.impl.bugs.services;

import com.ehb.connected.domain.impl.bugs.dto.BugCreateDto;
import com.ehb.connected.domain.impl.bugs.dto.BugDetailsDto;

import java.security.Principal;
import java.util.List;

public interface BugService {

    void create(Principal principal, BugCreateDto bugCreateDto);
    List<BugDetailsDto> getAll();
}
