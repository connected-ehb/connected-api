package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.auth.entities.LoginRequestDto;
import com.ehb.connected.domain.impl.auth.entities.RegistrationRequestDto;
import com.ehb.connected.domain.impl.canvas.CanvasAuthService;
import com.ehb.connected.domain.impl.invitations.services.InvitationService;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.auth.entities.CustomOAuth2User;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserDetailsMapper userDetailsMapper;
    private final OAuth2AuthorizedClientService authorizedClientService;
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
        // Get current authentication to determine if it's OAuth2
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
            String principalName = oauth2Token.getName();

            // Load the authorized client from Spring's storage
            OAuth2AuthorizedClient authorizedClient =
                    authorizedClientService.loadAuthorizedClient(registrationId, principalName);

            if (authorizedClient != null) {
                String accessToken = authorizedClient.getAccessToken().getTokenValue();

                // Delete/revoke token on Canvas
                try {
                    canvasAuthService.deleteAccessToken(accessToken);
                    log.info("Revoked Canvas access token for user: {}", principalName);
                } catch (Exception e) {
                    log.error("Failed to revoke Canvas token for user: {}", principalName, e);
                }

                // Remove tokens from Spring's storage
                authorizedClientService.removeAuthorizedClient(registrationId, principalName);
                log.info("Removed OAuth2 authorized client for user: {}", principalName);
            }
        } else {
            // Handle non-OAuth2 logout (form-based login)
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            log.info("Standard logout for user: {}", user.getEmail());
        }
    }

    @Override
    public AuthUserDetailsDto getCurrentUser(HttpServletRequest request) {
        try {
            return refreshSessionIfStale(request);
        } catch (BaseRuntimeException ex) {
            log.debug("Session invalid or missing, checking remember-me...");

            Optional<User> rememberedUser = rememberMeService.validateRememberMeToken(request);

            if (rememberedUser.isPresent()) {
                return restoreOAuth2Session(rememberedUser.get(), request);
            }

            throw ex;
        }
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

    private AuthUserDetailsDto restoreOAuth2Session(User user, HttpServletRequest request) {
        log.info("Restoring OAuth2 session for user: {}", user.getEmail());

        // Check if OAuth2 tokens exist
        String canvasId = String.valueOf(user.getCanvasUserId());
        OAuth2AuthorizedClient authorizedClient =
                authorizedClientService.loadAuthorizedClient("canvas", canvasId);

        if (authorizedClient == null) {
            log.warn("OAuth2 tokens missing for user: {}", user.getEmail());
            throw new BaseRuntimeException("Session expired. Please log in again.", HttpStatus.UNAUTHORIZED);
        }

        // ✅ Reconstruct Canvas-like attributes from database
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", user.getCanvasUserId());
        attributes.put("name", user.getFirstName() + " " + user.getLastName());
        attributes.put("short_name", user.getFirstName());
        attributes.put("sortable_name", user.getLastName() + ", " + user.getFirstName());
        if (user.getProfileImageUrl() != null) {
            attributes.put("avatar_url", user.getProfileImageUrl());
        }
        if (user.getEmail() != null) {
            attributes.put("primary_email", user.getEmail());
        }
        attributes.put("locale", "en");

        CustomOAuth2User principal = new CustomOAuth2User(user, attributes, "id");

        // Verify principal name
        String principalName = principal.getName();
        if (!principalName.equals(canvasId)) {
            log.error("Principal name mismatch: {} vs {}", principalName, canvasId);
            throw new BaseRuntimeException("Session restoration failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Create authentication
        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "canvas"
        );

        // Store in context and session
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        log.info("Successfully restored OAuth2 session for user: {}", user.getEmail());
        return userDetailsMapper.toDtoWithPrincipal(user, principal);
    }
}