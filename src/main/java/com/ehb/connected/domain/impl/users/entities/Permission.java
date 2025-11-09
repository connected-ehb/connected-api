package com.ehb.connected.domain.impl.users.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
    USER_READ("user:read"),
    USER_CREATE("user:create"),
    USER_ME_UPDATE("user:me:update"),
    USER_UPDATE("user:update"),
    USER_ME_REQUEST_DELETE("user:me:request_delete"),

    PROJECT_CREATE("project:create"),
    PROJECT_CREATE_GLOBAL("project:create_global"),
    PROJECT_READ("project:read"),
    PROJECT_UPDATE("project:update"),
    PROJECT_DELETE("project:delete"),
    PROJECT_PUBLISH("project:publish"),
    PROJECT_REMOVE_MEMBER("project:remove_member"),
    PROJECT_READ_ALL("project:read_all"),
    PROJECT_READ_PUBLISHED_OR_OWNED("project:read_published_or_owned"),
    PROJECT_CHANGE_STATUS("project:change_status"),
    PROJECT_APPLY("project:apply"),
    PROJECT_CLAIM("project:claim"),
    PROJECT_IMPORT("project:import"),
    PROJECT_LEAVE("project:leave"),

    EVENT_READ("event:read"),

    APPLICATION_CREATE("application:create"),
    APPLICATION_READ("application:read"),
    APPLICATION_READ_ALL("application:read_all"),
    APPLICATION_UPDATE("application:update"),
    APPLICATION_DELETE("application:delete"),
    APPLICATION_JOIN("application:join"),
    APPLICATION_REVIEW("application:review"),

    DISCUSSION_CREATE("discussion:create"),
    DISCUSSION_READ("discussion:read"),
    DISCUSSION_UPDATE("discussion:update"),
    DISCUSSION_DELETE("discussion:delete"),

    FEEDBACK_CREATE("feedback:create"),
    FEEDBACK_READ("feedback:read"),
    FEEDBACK_UPDATE("feedback:update"),
    FEEDBACK_DELETE("feedback:delete"),

    DEADLINE_CREATE("deadline:create"),
    DEADLINE_READ("deadline:read"),
    DEADLINE_UPDATE("deadline:update"),
    DEADLINE_DELETE("deadline:delete"),

    NOTIFICATION_READ("notification:read"),
    NOTIFICATION_UPDATE("notification:update"),
    NOTIFICATION_DELETE("notification:delete"),

    COURSE_CREATE("course:create"),
    COURSE_READ("course:read"),
    COURSE_READ_ALL("course:read_all"),
    COURSE_READ_ENROLLED("course:read_enrolled"),
    COURSE_UPDATE("course:update"),
    COURSE_DELETE("course:delete"),
    COURSE_REFRESH("course:refresh"),
    COURSE_VIEW_STUDENTS("course:view_students"),

    ASSIGNMENT_CREATE("assignment:create"),
    ASSIGNMENT_READ("assignment:read"),
    ASSIGNMENT_READ_ALL("assignment:read_all"),
    ASSIGNMENT_UPDATE("assignment:update"),
    ASSIGNMENT_DELETE("assignment:delete"),

    INVITATION_CREATE("invitation:create"),

    REVIEW_CREATE("review:create"),
    REVIEW_READ_ALL("review:read_all"),
    REVIEW_UPDATE("review:update"),
    REVIEW_DELETE("review:delete"),

    ANNOUNCEMENT_CREATE("announcement:create"),
    ANNOUNCEMENT_READ_ALL("announcement:read_all"),

    CANVAS_SYNC("canvas:sync"),

    DASHBOARD_READ("dashboard:read"),

    BUG_CREATE("bug:create"),
    BUG_READ_ALL("bug:read_all"),
    ;

    private final String permission;
}
