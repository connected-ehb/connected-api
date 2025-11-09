package com.ehb.connected.domain.impl.invitations.services;

import com.ehb.connected.domain.impl.invitations.entities.InvitationDetailsDto;
import org.springframework.security.core.Authentication;

public interface InvitationService {
    InvitationDetailsDto generateInvitation(Authentication authentication);
    boolean validateInvitationCode(String code);
    void markInvitationAsUsed(String code);
}
