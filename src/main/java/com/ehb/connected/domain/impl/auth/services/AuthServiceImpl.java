package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.auth.entities.AuthenticationType;
import com.ehb.connected.domain.impl.auth.entities.LoginRequestDto;
import com.ehb.connected.domain.impl.auth.entities.RegistrationRequestDto;
import com.ehb.connected.domain.impl.auth.entities.UserPrincipal;
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
import jakarta.servlet.http.HttpServletResponse;
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
    private final com.ehb.connected.domain.impl.users.Factories.UserFactory userFactory;
    private final PrincipalResolver principalResolver;

    @Override
    public UserDetailsDto register(RegistrationRequestDto request) {
        if (!invitationService.validateInvitationCode(request.getInvitationCode())) {
            throw new BaseRuntimeException("Invalid or expired invitation code.", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BaseRuntimeException("A user with this email already exists.", HttpStatus.CONFLICT);
        }

        // Create form user with emailVerified=true (researchers don't need verification for now)
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = userFactory.newFormUser(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                encodedPassword
        );

        user = userRepository.save(user);

        invitationService.markInvitationAsUsed(request.getInvitationCode());
        log.info("Registered new researcher user: {}", user.getEmail());
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

    /**
     * Performs logout operations for both OAuth2 and form-based authentication.
     * This method handles:
     * - Revoking Canvas OAuth2 tokens (if OAuth2 login)
     * - Removing OAuth2 authorized client from storage
     * - Clearing remember-me tokens
     * - Session invalidation is handled by Spring Security's logout filter
     *
     * @param authentication The current authentication object
     * @param response HTTP response for clearing cookies
     */
    @Override
    public void logout(Authentication authentication, HttpServletResponse response) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Logout called with no authenticated user");
            return;
        }

        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            handleOAuth2Logout(oauth2Token, response);
        } else {
            handleFormLoginLogout(authentication, response);
        }
    }

    /**
     * Handles logout for OAuth2 (Canvas) authenticated users.
     */
    private void handleOAuth2Logout(OAuth2AuthenticationToken oauth2Token, HttpServletResponse response) {
        String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
        String principalName = oauth2Token.getName();

        log.info("Processing OAuth2 logout for user: {} (registration: {})", principalName, registrationId);

        // Load the authorized client from Spring's storage
        OAuth2AuthorizedClient authorizedClient =
                authorizedClientService.loadAuthorizedClient(registrationId, principalName);

        if (authorizedClient != null) {
            String accessToken = authorizedClient.getAccessToken().getTokenValue();

            // Revoke token on Canvas
            try {
                canvasAuthService.deleteAccessToken(accessToken);
                log.info("Successfully revoked Canvas access token for user: {}", principalName);
            } catch (Exception e) {
                log.error("Failed to revoke Canvas token for user: {}", principalName, e);
                // Continue with logout even if revocation fails
            }

            // Remove tokens from Spring's OAuth2 storage
            authorizedClientService.removeAuthorizedClient(registrationId, principalName);
            log.info("Removed OAuth2 authorized client for user: {}", principalName);
        } else {
            log.warn("No OAuth2 authorized client found for user: {}", principalName);
        }

        // Clear remember-me token for OAuth2 users
        try {
            Long canvasUserId = Long.parseLong(principalName);
            userRepository.findByCanvasUserId(canvasUserId).ifPresent(user -> {
                rememberMeService.clearRememberMe(user, response);
                log.info("Cleared remember-me token for Canvas user: {}", canvasUserId);
            });
        } catch (NumberFormatException e) {
            log.error("Invalid Canvas user ID format: {}", principalName, e);
        }
    }

    /**
     * Handles logout for form-based (password) authenticated users.
     */
    private void handleFormLoginLogout(Authentication authentication, HttpServletResponse response) {
        String email = authentication.getName();
        log.info("Processing form login logout for user: {}", email);

        // Clear remember-me token for form users
        userRepository.findByEmail(email).ifPresent(user -> {
            rememberMeService.clearRememberMe(user, response);
            log.info("Cleared remember-me token for user: {}", email);
        });
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BaseRuntimeException("No authenticated user found", HttpStatus.UNAUTHORIZED);
        }

        // Extract the UserPrincipal using PrincipalResolver
        UserPrincipal sessionPrincipal = principalResolver.extractUserPrincipal(authentication);

        // Load fresh user data from database
        User dbUser = principalResolver.loadUserFromDatabase(sessionPrincipal);

        // Check if session is stale (role or emailVerified changed)
        if (!sessionPrincipal.matchesUser(dbUser)) {
            log.info("Refreshing stale session for user {} (role/emailVerified changed)", dbUser.getId());

            // Create fresh principal with updated data
            UserPrincipal freshPrincipal = UserPrincipal.fromUser(
                dbUser,
                sessionPrincipal.getAuthenticationType()
            );

            // Rebuild authentication based on type
            if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) oauth2Token.getPrincipal();

                // Create new CustomOAuth2User with fresh principal
                CustomOAuth2User newCustomOAuth2User = new CustomOAuth2User(
                    freshPrincipal,
                    customOAuth2User.getAttributes(),
                    customOAuth2User.getNameAttributeKey()
                );

                // Create new authentication
                OAuth2AuthenticationToken newAuth = new OAuth2AuthenticationToken(
                    newCustomOAuth2User,
                    newCustomOAuth2User.getAuthorities(),
                    oauth2Token.getAuthorizedClientRegistrationId()
                );

                // Update security context
                updateSecurityContext(newAuth, request);
                return userDetailsMapper.toDtoWithPrincipal(dbUser, newCustomOAuth2User);
            }
        }

        // Session is not stale, return current data
        // For OAuth2 users, extract the CustomOAuth2User principal
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token
            && oauth2Token.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
            return userDetailsMapper.toDtoWithPrincipal(dbUser, customOAuth2User);
        }

        // For form login users, just return user details without OAuth2 principal
        return userDetailsMapper.toDto(dbUser);
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

        // Create lightweight UserPrincipal for session
        UserPrincipal userPrincipal = UserPrincipal.fromUser(
            user,
            com.ehb.connected.domain.impl.auth.entities.AuthenticationType.OAUTH2
        );

        // Reconstruct Canvas-like attributes from database
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

        // Create CustomOAuth2User with lightweight principal
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userPrincipal, attributes, "id");

        // Verify principal name
        String principalName = customOAuth2User.getName();
        if (!principalName.equals(canvasId)) {
            log.error("Principal name mismatch: {} vs {}", principalName, canvasId);
            throw new BaseRuntimeException("Session restoration failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Create authentication
        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
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
        return userDetailsMapper.toDtoWithPrincipal(user, customOAuth2User);
    }
}