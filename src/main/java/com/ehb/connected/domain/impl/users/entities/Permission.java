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

    CREATE_APPLICATION("application:create"),
    READ_APPLICATION("application:read"),
    UPDATE_APPLICATION("application:update"),
    DELETE_APPLICATION("application:delete"),

    CREATE_DISCUSSION("discussion:create"),
    READ_DISCUSSION("discussion:read"),
    UPDATE_DISCUSSION("discussion:update"),
    DELETE_DISCUSSION("discussion:delete"),

    CREATE_FEEDBACK("feedback:create"),
    READ_FEEDBACK("feedback:read"),
    UPDATE_FEEDBACK("feedback:update"),
    DELETE_FEEDBACK("feedback:delete"),

    CREATE_COURSE("course:create"),
    READ_COURSE("course:read"),
    UPDATE_COURSE("course:update"),
    DELETE_COURSE("course:delete"),

    CREATE_ASSIGNMENT("assignment:create"),
    READ_ASSIGNMENT("assignment:read"),
    UPDATE_ASSIGNMENT("assignment:update"),
    DELETE_ASSIGNMENT("assignment:delete"),

    CREATE_INVITATION("invitation:create"),
    ;

    private final String permission;
}
