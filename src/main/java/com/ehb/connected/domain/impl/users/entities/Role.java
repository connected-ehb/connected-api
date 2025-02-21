package com.ehb.connected.domain.impl.users.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum Role implements GrantedAuthority {

    STUDENT(
            Set.of(
                    Permission.CREATE_PROJECT,
                    Permission.READ_PROJECT,
                    Permission.UPDATE_PROJECT,
                    Permission.DELETE_PROJECT,
                    Permission.CREATE_APPLICATION,
                    Permission.READ_APPLICATION,
                    Permission.UPDATE_APPLICATION,
                    Permission.DELETE_APPLICATION,
                    Permission.READ_FEEDBACK,
                    Permission.READ_COURSE,
                    Permission.READ_ASSIGNMENT
            )
    ),
    TEACHER(
            Set.of(
                    Permission.CREATE_PROJECT,
                    Permission.READ_PROJECT,
                    Permission.UPDATE_PROJECT,
                    Permission.DELETE_PROJECT,
                    Permission.CREATE_APPLICATION,
                    Permission.READ_APPLICATION,
                    Permission.UPDATE_APPLICATION,
                    Permission.DELETE_APPLICATION,
                    Permission.CREATE_DISCUSSION,
                    Permission.READ_DISCUSSION,
                    Permission.UPDATE_DISCUSSION,
                    Permission.DELETE_DISCUSSION,
                    Permission.CREATE_FEEDBACK,
                    Permission.READ_FEEDBACK,
                    Permission.UPDATE_FEEDBACK,
                    Permission.DELETE_FEEDBACK,
                    Permission.CREATE_COURSE,
                    Permission.READ_COURSE,
                    Permission.UPDATE_COURSE,
                    Permission.DELETE_COURSE,
                    Permission.CREATE_ASSIGNMENT,
                    Permission.READ_ASSIGNMENT,
                    Permission.UPDATE_ASSIGNMENT,
                    Permission.DELETE_ASSIGNMENT
            )
    ),
    ADMIN(
            Set.of(
                    Permission.CREATE_PROJECT,
                    Permission.READ_PROJECT,
                    Permission.UPDATE_PROJECT,
                    Permission.DELETE_PROJECT,
                    Permission.CREATE_APPLICATION,
                    Permission.READ_APPLICATION,
                    Permission.UPDATE_APPLICATION,
                    Permission.DELETE_APPLICATION,
                    Permission.CREATE_DISCUSSION,
                    Permission.READ_DISCUSSION,
                    Permission.UPDATE_DISCUSSION,
                    Permission.DELETE_DISCUSSION,
                    Permission.CREATE_FEEDBACK,
                    Permission.READ_FEEDBACK,
                    Permission.UPDATE_FEEDBACK,
                    Permission.DELETE_FEEDBACK,
                    Permission.CREATE_COURSE,
                    Permission.READ_COURSE,
                    Permission.UPDATE_COURSE,
                    Permission.DELETE_COURSE,
                    Permission.CREATE_ASSIGNMENT,
                    Permission.READ_ASSIGNMENT,
                    Permission.UPDATE_ASSIGNMENT,
                    Permission.DELETE_ASSIGNMENT
            )
    );

    private final Set<Permission> permissions;

    public Set<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Permission permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission.name()));
        }

        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }

    @Override
    public String getAuthority() {
        return name();
    }
}

