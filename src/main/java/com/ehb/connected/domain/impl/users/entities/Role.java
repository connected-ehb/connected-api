package com.ehb.connected.domain.impl.users.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
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
                    Permission.READ_ASSIGNMENT,
                    Permission.READ_PUBLISHED_OR_OWNED_PROJECTS,
                    Permission.PROJECT_APPLY,
                    Permission.JOIN_APPLICATION,
                    Permission.REVIEW_APPLICATION,
                    Permission.READ_ENROLLED_COURSES,
                    Permission.READ_DEADLINE,
                    Permission.READ_NOTIFICATION,
                    Permission.UPDATE_NOTIFICATION,
                    Permission.DELETE_NOTIFICATION,
                    Permission.READ_ALL_APPLICATIONS,
                    Permission.PROJECT_CLAIM
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
                    Permission.DELETE_ASSIGNMENT,
                    Permission.REMOVE_MEMBER,
                    Permission.PUBLISH_PROJECT,
                    Permission.READ_ALL_PROJECTS,
                    Permission.READ_PUBLISHED_OR_OWNED_PROJECTS,
                    Permission.CHANGE_PROJECT_STATUS,
                    Permission.READ_ALL_APPLICATIONS,
                    Permission.SYNC_CANVAS,
                    Permission.VIEW_STUDENT_COURSE,
                    Permission.READ_ALL_COURSES,
                    Permission.READ_ALL_ASSIGNMENTS,
                    Permission.READ_ENROLLED_COURSES,
                    Permission.CREATE_DEADLINE,
                    Permission.READ_DEADLINE,
                    Permission.UPDATE_DEADLINE,
                    Permission.DELETE_DEADLINE,
                    Permission.READ_NOTIFICATION,
                    Permission.UPDATE_NOTIFICATION,
                    Permission.DELETE_NOTIFICATION,
                    Permission.CREATE_REVIEW,
                    Permission.READ_ALL_REVIEW,
                    Permission.UPDATE_REVIEW,
                    Permission.DELETE_REVIEW
            )
    ),
    RESEARCHER(
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
                    Permission.DELETE_FEEDBACK
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
                    Permission.DELETE_ASSIGNMENT,
                    Permission.REMOVE_MEMBER,
                    Permission.PUBLISH_PROJECT,
                    Permission.READ_ALL_PROJECTS,
                    Permission.READ_PUBLISHED_OR_OWNED_PROJECTS,
                    Permission.CHANGE_PROJECT_STATUS,
                    Permission.READ_ALL_APPLICATIONS,
                    Permission.SYNC_CANVAS,
                    Permission.VIEW_STUDENT_COURSE,
                    Permission.READ_ALL_COURSES,
                    Permission.READ_ALL_ASSIGNMENTS,
                    Permission.READ_ENROLLED_COURSES,
                    Permission.CREATE_DEADLINE,
                    Permission.READ_DEADLINE,
                    Permission.UPDATE_DEADLINE,
                    Permission.DELETE_DEADLINE,
                    Permission.READ_NOTIFICATION,
                    Permission.UPDATE_NOTIFICATION,
                    Permission.DELETE_NOTIFICATION,
                    Permission.CREATE_REVIEW,
                    Permission.READ_ALL_REVIEW,
                    Permission.UPDATE_REVIEW,
                    Permission.DELETE_REVIEW
            )
    );

    private final Set<Permission> permissions;

    public Set<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Permission permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission.getPermission()));
        }

        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }

    @Override
    public String getAuthority() {
        return name();
    }
}

