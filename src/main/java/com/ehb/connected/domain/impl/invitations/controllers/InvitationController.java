package com.ehb.connected.domain.impl.invitations.controllers;

import com.ehb.connected.domain.impl.invitations.entities.InvitationDetailsDto;
import com.ehb.connected.domain.impl.invitations.services.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PreAuthorize("hasAnyAuthority('invitation:create')")
    @PostMapping("/generate")
    public ResponseEntity<InvitationDetailsDto> generateInvitation(Principal principal) {
        InvitationDetailsDto code = invitationService.generateInvitation(principal);
        return ResponseEntity.ok(code);
    }
}
