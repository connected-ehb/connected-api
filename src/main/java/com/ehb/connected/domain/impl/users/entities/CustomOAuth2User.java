package com.ehb.connected.domain.impl.users.entities;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Session principal for OAuth2 logins that wraps your JPA User entity,
 * while still exposing OAuth2 attributes. Keeps DB as source-of-truth.
 */
@Getter
public class CustomOAuth2User implements OAuth2User, UserDetails, Serializable {
    private static final long serialVersionUID = 1L;

    private final User user;
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    public CustomOAuth2User(User user, Map<String, Object> attributes, String nameAttributeKey) {
        this.user = Objects.requireNonNull(user, "user must not be null");
        // sanitize attributes: remove null values and keys
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

    // ---------- OAuth2User ----------
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        Object val = attributes.getOrDefault(nameAttributeKey, user.getCanvasUserId());
        return val == null ? "" : String.valueOf(val);
    }

    // ---------- UserDetails ----------
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return user.getAuthorities(); }
    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getEmail() != null ? user.getEmail() : getName(); }
    @Override public boolean isAccountNonExpired() { return user.isAccountNonExpired(); }
    @Override public boolean isAccountNonLocked() { return user.isAccountNonLocked(); }
    @Override public boolean isCredentialsNonExpired() { return user.isCredentialsNonExpired(); }
    @Override public boolean isEnabled() { return user.isEnabled(); }
}
