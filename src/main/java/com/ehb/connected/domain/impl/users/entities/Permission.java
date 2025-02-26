package com.ehb.connected.domain.impl.users.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
    CREATE_PROJECT("project:create"),
    READ_PROJECT("project:read"),
    UPDATE_PROJECT("project:update"),
    DELETE_PROJECT("project:delete"),
    PUBLISH_PROJECT("project:publish"),
    REMOVE_MEMBER("project:remove_member"),
    READ_ALL_PROJECTS("project:read_all"),
    READ_PUBLISHED_OR_OWNED_PROJECTS("project:read_published_or_owned"),
    CHANGE_PROJECT_STATUS("project:change_status"),
    PROJECT_APPLY("project:apply"),
    PROJECT_CLAIM("project:claim"),

    CREATE_APPLICATION("application:create"),
    READ_APPLICATION("application:read"),
    READ_ALL_APPLICATIONS("application:read_all"),
    UPDATE_APPLICATION("application:update"),
    DELETE_APPLICATION("application:delete"),
    JOIN_APPLICATION("application:join"),
    REVIEW_APPLICATION("application:review"),

    CREATE_DISCUSSION("discussion:create"),
    READ_DISCUSSION("discussion:read"),
    UPDATE_DISCUSSION("discussion:update"),
    DELETE_DISCUSSION("discussion:delete"),

    CREATE_FEEDBACK("feedback:create"),
    READ_FEEDBACK("feedback:read"),
    UPDATE_FEEDBACK("feedback:update"),
    DELETE_FEEDBACK("feedback:delete"),

    CREATE_DEADLINE("deadline:create"),
    READ_DEADLINE("deadline:read"),
    UPDATE_DEADLINE("deadline:update"),
    DELETE_DEADLINE("deadline:delete"),

    READ_NOTIFICATION("notification:read"),
    UPDATE_NOTIFICATION("notification:update"),
    DELETE_NOTIFICATION("notification:delete"),

    CREATE_COURSE("course:create"),
    READ_COURSE("course:read"),
    READ_ALL_COURSES("course:read_all"),
    READ_ENROLLED_COURSES("course:read_enrolled"),
    UPDATE_COURSE("course:update"),
    DELETE_COURSE("course:delete"),
    VIEW_STUDENT_COURSE("course:view_students"),

    CREATE_ASSIGNMENT("assignment:create"),
    READ_ASSIGNMENT("assignment:read"),
    READ_ALL_ASSIGNMENTS("assignment:read_all"),
    UPDATE_ASSIGNMENT("assignment:update"),
    DELETE_ASSIGNMENT("assignment:delete"),

    CREATE_INVITATION("invitation:create"),

    CREATE_REVIEW("review:create"),
    READ_ALL_REVIEW("review:read_all"),
    UPDATE_REVIEW("review:update"),
    DELETE_REVIEW("review:delete"),


    SYNC_CANVAS("canvas:sync"),
    ;

    private final String permission;
}
