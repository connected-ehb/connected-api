package com.ehb.connected.domain.impl.bugs.services;

import com.ehb.connected.domain.impl.bugs.dto.BugCreateDto;

import java.security.Principal;

public interface BugService {

    void create(Principal principal, BugCreateDto bugCreateDto);
}
