package com.ehb.connected.domain.impl.auth.services;

import com.ehb.connected.domain.impl.auth.dto.LoginRequest;
import com.ehb.connected.domain.impl.auth.dto.RegistrationRequest;
import com.ehb.connected.domain.impl.auth.security.AuthenticationType;
import com.ehb.connected.domain.impl.auth.security.CustomOAuth2User;
import com.ehb.connected.domain.impl.auth.security.UserPrincipal;
import com.ehb.connected.domain.impl.canvas.CanvasAuthService;
import com.ehb.connected.domain.impl.invitations.services.InvitationService;
import com.ehb.connected.domain.impl.users.Factories.UserFactory;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
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

import java.util.Map;
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
    private final UserFactory userFactory;
    private final PrincipalResolver principalResolver;

    @Override
    public UserDetailsDto register(RegistrationRequest request) {
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

    /**
     * Authenticates a user with email and password (form-based login).
     * Creates a Spring Security session on successful authentication.
     *
     * @param request Login credentials (email + password)
     * @param httpRequest HTTP request to create session
     * @param httpResponse HTTP response to set remember-me cookie if needed
     * @return User details after successful authentication
     * @throws EntityNotFoundException if user not found
     * @throws BaseRuntimeException if credentials are invalid
     */
    @Override
    public UserDetailsDto login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        // 1. Validate credentials
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BaseRuntimeException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        if (!user.isEnabled()) {
            throw new BaseRuntimeException("Account is disabled", HttpStatus.FORBIDDEN);
        }

        // 2. Create Spring Security authentication
        Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                user,  // principal
                user.getPassword(),  // credentials
                user.getAuthorities()  // authorities
        );

        // 3. Set security context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 4. Save to HTTP session
        httpRequest.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        // 5. Create remember-me token if requested (optional - you can add a checkbox in frontend)
        // rememberMeService.createRememberMeToken(user, httpResponse);

        log.info("User {} successfully logged in via form authentication", user.getEmail());
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
                return restoreSession(rememberedUser.get(), request);
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

    /**
     * Restores a user session from remember-me token.
     * Supports both OAuth2 (Canvas) and form-based (researcher) authentication.
     * <p>
     * Security notes:
     * - For OAuth2: Validates that Canvas tokens still exist (basic check)
     * - For form login: Creates simple authentication
     * - Does NOT recreate fake OAuth2 attributes
     * - User should re-login if OAuth2 tokens are expired/revoked
     */
    private AuthUserDetailsDto restoreSession(User user, HttpServletRequest request) {
        log.info("Restoring session for user: {} (id: {})", user.getEmail(), user.getId());

        // Determine authentication type
        boolean isOAuth2User = user.getCanvasUserId() != null;

        if (isOAuth2User) {
            return restoreOAuth2UserSession(user, request);
        } else {
            return restoreFormUserSession(user, request);
        }
    }

    /**
     * Restores session for OAuth2 (Canvas) users.
     * Only proceeds if OAuth2 tokens exist in the oauth2_authorized_client table.
     */
    private AuthUserDetailsDto restoreOAuth2UserSession(User user, HttpServletRequest request) {
        String canvasId = String.valueOf(user.getCanvasUserId());

        // Check if OAuth2 tokens exist
        OAuth2AuthorizedClient authorizedClient =
                authorizedClientService.loadAuthorizedClient("canvas", canvasId);

        if (authorizedClient == null) {
            log.warn("OAuth2 tokens missing for Canvas user {}. Require re-login.", user.getCanvasUserId());
            throw new BaseRuntimeException(
                    "Your Canvas session has expired. Please log in again.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        // Tokens exist - create minimal authentication
        // Note: We don't validate the token with Canvas here for performance.
        // If it's expired, it will fail on next Canvas API call.
        UserPrincipal userPrincipal = UserPrincipal.fromUser(user, AuthenticationType.OAUTH2);

        // Create minimal OAuth2 attributes (just the essentials)
        Map<String, Object> attributes = Map.of(
                "id", user.getCanvasUserId()
        );

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userPrincipal, attributes, "id");

        // Create OAuth2 authentication
        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                customOAuth2User,
                user.getAuthorities(),
                "canvas"
        );

        // Set security context
        updateSecurityContext(auth, request);

        log.info("Restored OAuth2 session for Canvas user: {}", user.getCanvasUserId());
        return userDetailsMapper.toDtoWithPrincipal(user, customOAuth2User);
    }

    /**
     * Restores session for form-based (researcher) users.
     * Creates simple username/password authentication.
     */
    private AuthUserDetailsDto restoreFormUserSession(User user, HttpServletRequest request) {
        // Create simple authentication (no OAuth2 complexity needed)
        UserPrincipal userPrincipal = UserPrincipal.fromUser(user, AuthenticationType.FORM);

        // Use UsernamePasswordAuthenticationToken for form users
        Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                user,  // principal
                null,  // credentials (already authenticated via remember-me)
                user.getAuthorities()
        );

        // Set security context
        updateSecurityContext(auth, request);

        log.info("Restored form login session for user: {}", user.getEmail());
        return userDetailsMapper.toDto(user);
    }
}