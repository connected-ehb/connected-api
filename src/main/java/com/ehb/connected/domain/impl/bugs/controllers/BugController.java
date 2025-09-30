package com.ehb.connected.domain.impl.bugs.controllers;

import com.ehb.connected.domain.impl.bugs.dto.BugCreateDto;
import com.ehb.connected.domain.impl.bugs.services.BugService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createBug(Principal principal, @RequestBody @Valid BugCreateDto bug) {
        bugService.create(principal, bug);
    }
}
