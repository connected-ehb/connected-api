package com.ehb.connected.domain.impl.users.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import static com.ehb.connected.domain.impl.users.entities.Permission.*;

@Getter
@RequiredArgsConstructor
public enum Role implements GrantedAuthority {

    STUDENT(
            Set.of(
                    PROJECT_CREATE,
                    PROJECT_READ,
                    PROJECT_UPDATE,
                    PROJECT_DELETE,
                    APPLICATION_CREATE,
                    APPLICATION_READ,
                    APPLICATION_UPDATE,
                    APPLICATION_DELETE,
                    FEEDBACK_READ,
                    COURSE_READ,
                    ASSIGNMENT_READ,
                    PROJECT_READ_PUBLISHED_OR_OWNED,
                    PROJECT_APPLY,
                    PROJECT_LEAVE,
                    APPLICATION_JOIN,
                    APPLICATION_REVIEW,
                    COURSE_READ_ENROLLED,
                    DEADLINE_READ,
                    NOTIFICATION_READ,
                    NOTIFICATION_UPDATE,
                    NOTIFICATION_DELETE,
                    APPLICATION_READ_ALL,
                    PROJECT_CLAIM,
                    PROJECT_IMPORT,
                    ANNOUNCEMENT_READ_ALL,
                    BUG_CREATE,
                    USER_READ,
                    USER_ME_UPDATE,
                    USER_ME_REQUEST_DELETE
            )
    ),
    TEACHER(
            Set.of(
                    PROJECT_CREATE,
                    PROJECT_READ,
                    PROJECT_UPDATE,
                    PROJECT_DELETE,
                    APPLICATION_CREATE,
                    APPLICATION_READ,
                    APPLICATION_UPDATE,
                    APPLICATION_DELETE,
                    DISCUSSION_CREATE,
                    DISCUSSION_READ,
                    DISCUSSION_UPDATE,
                    DISCUSSION_DELETE,
                    FEEDBACK_CREATE,
                    FEEDBACK_READ,
                    FEEDBACK_UPDATE,
                    FEEDBACK_DELETE,
                    COURSE_CREATE,
                    COURSE_READ,
                    COURSE_UPDATE,
                    COURSE_DELETE,
                    ASSIGNMENT_CREATE,
                    ASSIGNMENT_READ,
                    ASSIGNMENT_UPDATE,
                    ASSIGNMENT_DELETE,
                    PROJECT_REMOVE_MEMBER,
                    PROJECT_PUBLISH,
                    PROJECT_READ_ALL,
                    PROJECT_READ_PUBLISHED_OR_OWNED,
                    PROJECT_CHANGE_STATUS,
                    APPLICATION_READ_ALL,
                    CANVAS_SYNC,
                    COURSE_VIEW_STUDENTS,
                    COURSE_READ_ALL,
                    COURSE_REFRESH,
                    ASSIGNMENT_READ_ALL,
                    COURSE_READ_ENROLLED,
                    DEADLINE_CREATE,
                    DEADLINE_READ,
                    DEADLINE_UPDATE,
                    DEADLINE_DELETE,
                    NOTIFICATION_READ,
                    NOTIFICATION_UPDATE,
                    NOTIFICATION_DELETE,
                    REVIEW_CREATE,
                    REVIEW_READ_ALL,
                    REVIEW_UPDATE,
                    REVIEW_DELETE,
                    ANNOUNCEMENT_CREATE,
                    ANNOUNCEMENT_READ_ALL,
                    INVITATION_CREATE,
                    DASHBOARD_READ,
                    BUG_CREATE,
                    BUG_READ_ALL,
                    USER_READ,
                    USER_ME_UPDATE,
                    USER_ME_REQUEST_DELETE,
                    EVENT_READ
            )
    ),
    RESEARCHER(
            Set.of(
                    PROJECT_CREATE,
                    PROJECT_READ,
                    PROJECT_UPDATE,
                    PROJECT_DELETE,
                    APPLICATION_CREATE,
                    APPLICATION_READ,
                    APPLICATION_UPDATE,
                    APPLICATION_DELETE,
                    DISCUSSION_CREATE,
                    DISCUSSION_READ,
                    DISCUSSION_UPDATE,
                    DISCUSSION_DELETE,
                    FEEDBACK_CREATE,
                    FEEDBACK_READ,
                    FEEDBACK_UPDATE,
                    FEEDBACK_DELETE,
                    PROJECT_CREATE_GLOBAL,
                    NOTIFICATION_READ,
                    NOTIFICATION_UPDATE,
                    NOTIFICATION_DELETE,
                    BUG_CREATE,
                    USER_READ,
                    USER_ME_UPDATE,
                    USER_ME_REQUEST_DELETE
            )
    ),
    ADMIN(
            Set.of(
                    PROJECT_CREATE,
                    PROJECT_READ,
                    PROJECT_UPDATE,
                    PROJECT_DELETE,
                    APPLICATION_CREATE,
                    APPLICATION_READ,
                    APPLICATION_UPDATE,
                    APPLICATION_DELETE,
                    DISCUSSION_CREATE,
                    DISCUSSION_READ,
                    DISCUSSION_UPDATE,
                    DISCUSSION_DELETE,
                    FEEDBACK_CREATE,
                    FEEDBACK_READ,
                    FEEDBACK_UPDATE,
                    FEEDBACK_DELETE,
                    COURSE_CREATE,
                    COURSE_READ,
                    COURSE_UPDATE,
                    COURSE_DELETE,
                    ASSIGNMENT_CREATE,
                    ASSIGNMENT_READ,
                    ASSIGNMENT_UPDATE,
                    ASSIGNMENT_DELETE,
                    PROJECT_REMOVE_MEMBER,
                    PROJECT_PUBLISH,
                    PROJECT_READ_ALL,
                    PROJECT_READ_PUBLISHED_OR_OWNED,
                    PROJECT_CHANGE_STATUS,
                    APPLICATION_READ_ALL,
                    CANVAS_SYNC,
                    COURSE_VIEW_STUDENTS,
                    COURSE_READ_ALL,
                    ASSIGNMENT_READ_ALL,
                    COURSE_READ_ENROLLED,
                    DEADLINE_CREATE,
                    DEADLINE_READ,
                    DEADLINE_UPDATE,
                    DEADLINE_DELETE,
                    NOTIFICATION_READ,
                    NOTIFICATION_UPDATE,
                    NOTIFICATION_DELETE,
                    REVIEW_CREATE,
                    REVIEW_READ_ALL,
                    REVIEW_UPDATE,
                    REVIEW_DELETE,
                    BUG_CREATE,
                    BUG_READ_ALL,
                    USER_READ,
                    USER_CREATE,
                    USER_ME_UPDATE,
                    USER_UPDATE,
                    USER_ME_REQUEST_DELETE,
                    EVENT_READ
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

