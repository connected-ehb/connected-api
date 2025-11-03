package com.ehb.connected.domain.impl.auth.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * OAuth2 session principal that wraps a lightweight UserPrincipal.
 * Implements Spring Security's OAuth2User interface for OAuth2 authentication.
 * Stores minimal user data in Redis session to avoid serialization issues.
 */
@Getter
public class CustomOAuth2User implements OAuth2User, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final UserPrincipal userPrincipal;
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    /**
     * Creates a new CustomOAuth2User wrapping a lightweight UserPrincipal.
     * Constructor annotated with @JsonCreator for Jackson deserialization from Redis.
     */
    @JsonCreator
    public CustomOAuth2User(
            @JsonProperty("userPrincipal") UserPrincipal userPrincipal,
            @JsonProperty("attributes") Map<String, Object> attributes,
            @JsonProperty("nameAttributeKey") String nameAttributeKey) {

        this.userPrincipal = Objects.requireNonNull(userPrincipal, "userPrincipal must not be null");

        // Sanitize attributes: remove null keys/values
        if (attributes == null) {
            this.attributes = Collections.emptyMap();
        } else {
            this.attributes = attributes.entrySet().stream()
                    .filter(e -> e.getKey() != null && e.getValue() != null)
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        this.nameAttributeKey = (nameAttributeKey == null || nameAttributeKey.isBlank()) ? "id" : nameAttributeKey;
    }

    @JsonIgnore
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Returns the Canvas user ID as a String.
     * Must match the principal name stored in oauth2_authorized_client table.
     */
    @JsonIgnore
    @Override
    public String getName() {
        return userPrincipal.getName();
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userPrincipal.getAuthorities();
    }

    /**
     * Checks if this principal matches another UserPrincipal.
     * Used for detecting stale session data that needs refreshing.
     */
    public boolean matches(UserPrincipal other) {
        if (other == null) {
            return false;
        }
        return this.userPrincipal.equals(other)
                && Objects.equals(this.userPrincipal.getRole(), other.getRole())
                && this.userPrincipal.isEmailVerified() == other.isEmailVerified();
    }
}
