package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.auth.entities.LoginRequestDto;
import com.ehb.connected.domain.impl.auth.entities.RegistrationRequestDto;
import com.ehb.connected.domain.impl.canvas.CanvasAuthService;
import com.ehb.connected.domain.impl.invitations.services.InvitationService;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.CustomOAuth2User;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.ehb.connected.domain.impl.users.services.UserService;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserDetailsMapper userDetailsMapper;
    private final CanvasAuthService canvasAuthService;
    private final InvitationService invitationService;
    private final PasswordEncoder passwordEncoder;
    private final RememberMeService rememberMeService;

    @Override
    public UserDetailsDto register(RegistrationRequestDto request) {
        if (!invitationService.validateInvitationCode(request.getInvitationCode())) {
            throw new BaseRuntimeException("Invalid or expired invitation code.", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BaseRuntimeException("A user with this email already exists.", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.RESEARCHER);
        user = userRepository.save(user);

        invitationService.markInvitationAsUsed(request.getInvitationCode());
        return userDetailsMapper.toUserDetailsDto(user);
    }

    @Override
    public UserDetailsDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BaseRuntimeException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        return userDetailsMapper.toUserDetailsDto(user);
    }

    @Override
    public void logout(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (user.getAccessToken() != null) {
            canvasAuthService.deleteAccessToken(user.getAccessToken());
        }
        user.setAccessToken(null);
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    /**
     * Retrieves the current authenticated user.
     * If the session expired, tries to restore it using the remember-me cookie.
     */
    @Override
    public AuthUserDetailsDto getCurrentUser(HttpServletRequest request) {
        try {
            return refreshSessionIfStale(request);
        } catch (BaseRuntimeException ex) {
            log.debug("Session invalid or missing, trying remember-me restoration...");
            return rememberMeService.validateRememberMeToken(request)
                    .map(user -> restoreSessionFromUser(user, request))
                    .orElseThrow(() -> ex);
        }
    }

    private AuthUserDetailsDto restoreSessionFromUser(User user, HttpServletRequest request) {
        log.info("Restoring session for remembered user [{}]", user.getEmail());

        CustomOAuth2User principal = new CustomOAuth2User(
                user,
                Map.of("id", user.getCanvasUserId()),
                "id"
        );

        Authentication auth = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "canvas"
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        return userDetailsMapper.toDtoWithPrincipal(user, principal);
    }

    @Override
    public AuthUserDetailsDto refreshSessionIfStale(HttpServletRequest request) {
        Authentication authentication = getCurrentAuthentication();
        CustomOAuth2User customUser = extractCustomUser(authentication);
        User dbUser = loadDbUser(customUser);

        if (isStale(customUser.getUser(), dbUser)) {
            log.info("Refreshing session for user {} (role/emailVerified changed)", dbUser.getId());
            CustomOAuth2User newPrincipal = rebuildPrincipal(dbUser, customUser);
            Authentication newAuth = rebuildAuthentication(newPrincipal, (OAuth2AuthenticationToken) authentication);
            updateSecurityContext(newAuth, request);
            return userDetailsMapper.toDtoWithPrincipal(dbUser, newPrincipal);
        }

        return userDetailsMapper.toDtoWithPrincipal(dbUser, customUser);
    }

    private Authentication getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            throw new BaseRuntimeException("No OAuth2 authentication found", HttpStatus.UNAUTHORIZED);
        }
        return authentication;
    }

    private CustomOAuth2User extractCustomUser(Authentication authentication) {
        OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) authentication;
        if (!(oauth2Auth.getPrincipal() instanceof CustomOAuth2User customUser)) {
            throw new BaseRuntimeException("Unexpected principal type", HttpStatus.UNAUTHORIZED);
        }
        return customUser;
    }

    private User loadDbUser(CustomOAuth2User customUser) {
        return userRepository.findByCanvasUserId(customUser.getUser().getCanvasUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private boolean isStale(User sessionUser, User dbUser) {
        boolean roleChanged = !Objects.equals(dbUser.getRole(), sessionUser.getRole());
        boolean emailVerifiedChanged = dbUser.isEmailVerified() != sessionUser.isEmailVerified();
        return roleChanged || emailVerifiedChanged;
    }

    private CustomOAuth2User rebuildPrincipal(User dbUser, CustomOAuth2User oldPrincipal) {
        return new CustomOAuth2User(
                dbUser,
                oldPrincipal.getAttributes(),
                oldPrincipal.getNameAttributeKey()
        );
    }

    private Authentication rebuildAuthentication(CustomOAuth2User newPrincipal, OAuth2AuthenticationToken oldAuth) {
        return new OAuth2AuthenticationToken(
                newPrincipal,
                newPrincipal.getAuthorities(),
                oldAuth.getAuthorizedClientRegistrationId()
        );
    }

    private void updateSecurityContext(Authentication newAuth, HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(newAuth);
        SecurityContextHolder.setContext(context);

        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );
    }
}
