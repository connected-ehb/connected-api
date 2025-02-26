package com.ehb.connected.domain.impl.invitations.services;

import com.ehb.connected.domain.impl.invitations.entities.Invitation;
import com.ehb.connected.domain.impl.invitations.entities.InvitationDetailsDto;
import com.ehb.connected.domain.impl.invitations.repositories.InvitationRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService{
    private final InvitationRepository invitationRepository;
    private final UserService userService;
    Logger logger = LoggerFactory.getLogger(InvitationServiceImpl.class);

    @Override
    public InvitationDetailsDto generateInvitation(User user) {
        String code = generateSecureCode(26);
        Invitation invitation = Invitation.builder()
                .code(code)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(72))
                .used(false)
                .build();
        invitation.setCreatedBy(user);
        invitationRepository.save(invitation);

        InvitationDetailsDto invitationDetailsDto = new InvitationDetailsDto();
        invitationDetailsDto.setCode(code);
        logger.info("[{}] Generated invitation code", InvitationService.class.getSimpleName());
        return invitationDetailsDto;
    }

    private String generateSecureCode(int length) {
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    @Override
    public boolean validateInvitationCode(String code) {
        Optional<Invitation> invitationOpt = invitationRepository.findByCode(code);
        if (invitationOpt.isPresent()) {
            Invitation invitation = invitationOpt.get();
            // Check if already used or expired
            return !invitation.isUsed() && !invitation.getExpiresAt().isBefore(LocalDateTime.now());
        }
        return false;
    }

    @Override
    public void markInvitationAsUsed(String code) {
        Invitation invitation = invitationRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation code"));
        invitation.setUsed(true);
        invitationRepository.save(invitation);
    }
}
