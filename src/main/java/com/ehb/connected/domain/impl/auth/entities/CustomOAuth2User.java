package com.ehb.connected.domain.impl.auth.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Session principal for OAuth2 logins that wraps a lightweight UserPrincipal.
 * Stores minimal user data in Redis session to avoid serialization issues with JPA entities.
 * The database is the source of truth - this is just a session cache.
 */
@Getter
public class CustomOAuth2User implements OAuth2User, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Lightweight user principal (stored in Redis).
     * Contains only essential user data (~200 bytes).
     */
    private final UserPrincipal userPrincipal;

    /**
     * OAuth2 attributes from Canvas (user info endpoint response).
     * Sanitized to remove null keys/values.
     */
    private final Map<String, Object> attributes;

    /**
     * The attribute key used as the user name (typically "id" for Canvas).
     */
    private final String nameAttributeKey;

    /**
     * Creates a new CustomOAuth2User wrapping a lightweight UserPrincipal.
     * Constructor annotated with @JsonCreator for Jackson deserialization from Redis.
     *
     * @param userPrincipal The lightweight user principal
     * @param attributes OAuth2 attributes from provider
     * @param nameAttributeKey The attribute key to use as name
     */
    @JsonCreator
    public CustomOAuth2User(
            @JsonProperty("userPrincipal") UserPrincipal userPrincipal,
            @JsonProperty("attributes") Map<String, Object> attributes,
            @JsonProperty("nameAttributeKey") String nameAttributeKey) {
        this.userPrincipal = Objects.requireNonNull(userPrincipal, "userPrincipal must not be null");

        // Sanitize attributes: remove null values and keys
        if (attributes == null) {
            this.attributes = Collections.emptyMap();
        } else {
            this.attributes = attributes.entrySet().stream()
                    .filter(e -> e.getKey() != null && e.getValue() != null)
                    .collect(Collectors.toUnmodifiableMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    ));
        }

        this.nameAttributeKey = (nameAttributeKey == null || nameAttributeKey.isBlank()) ? "id" : nameAttributeKey;
    }

    // ---------- OAuth2User Implementation ----------

    @JsonIgnore
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Returns the Canvas user ID as a String.
     * This is used as the principal name for OAuth2AuthorizedClient storage.
     * Must match the value stored in oauth2_authorized_client.principal_name.
     */
    @JsonIgnore
    @Override
    public String getName() {
        // Use the UserPrincipal's getName() method
        // For OAuth2 users, this returns the Canvas user ID
        return userPrincipal.getName();
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userPrincipal.getAuthorities();
    }

    /**
     * Checks if this principal represents the same user as a UserPrincipal.
     * Used for detecting stale session data.
     *
     * @param other The other UserPrincipal
     * @return true if they represent the same user with same state
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
