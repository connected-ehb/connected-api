package com.ehb.connected.domain.impl.invitations.services;

import com.ehb.connected.domain.impl.invitations.entities.InvitationDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;

import java.security.Principal;

public interface InvitationService {
    InvitationDetailsDto generateInvitation(User principal);
    boolean validateInvitationCode(String code);
    void markInvitationAsUsed(String code);
}
