package com.ehb.connected.domain.impl.invitations.services;

import com.ehb.connected.domain.impl.invitations.entities.InvitationDetailsDto;

import java.security.Principal;

public interface InvitationService {
    InvitationDetailsDto generateInvitation(Principal principal);
    boolean validateInvitationCode(String code);
    void markInvitationAsUsed(String code);
}
