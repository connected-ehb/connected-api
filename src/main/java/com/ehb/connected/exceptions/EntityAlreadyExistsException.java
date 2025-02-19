package com.ehb.connected.exceptions;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

public class EntityAlreadyExistsException extends BaseRuntimeException {

    private static final String BASE_MESSAGE = "already exists with identifier:";
    private static final HttpStatus BASE_HTTP_STATUS = HttpStatus.CONFLICT;

    public EntityAlreadyExistsException(@NotNull String message) {
        super(message, BASE_HTTP_STATUS);
    }

    public EntityAlreadyExistsException(@NotNull Class<?> clazz, @NotNull Long id, @NotNull String name) {
        this("%s %s %s and name: %s".formatted(clazz.getSimpleName(), BASE_MESSAGE, id.toString(), name));
    }

    public static Supplier<EntityAlreadyExistsException> error(String message) {
        return () -> new EntityAlreadyExistsException(message);
    }

    public static Supplier<EntityAlreadyExistsException> error(Class<?> classType, Object identifier) {
        return () -> new EntityAlreadyExistsException("%s %s %s".formatted(classType.getSimpleName(), BASE_MESSAGE, identifier.toString()));
    }
}
