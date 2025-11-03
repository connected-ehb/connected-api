package com.ehb.connected.domain.impl.invitations.controllers;

import com.ehb.connected.domain.impl.invitations.entities.InvitationDetailsDto;
import com.ehb.connected.domain.impl.invitations.services.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PreAuthorize("hasAnyAuthority('invitation:create')")
    @PostMapping("/generate")
    public ResponseEntity<InvitationDetailsDto> generateInvitation(Authentication authentication) {
        InvitationDetailsDto code = invitationService.generateInvitation(authentication);
        return ResponseEntity.ok(code);
    }
}
