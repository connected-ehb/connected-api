package com.ehb.connected.exceptions;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

public class EntityNotFoundException extends BaseRuntimeException {

    private static final String BASE_MESSAGE = "not found for id: ";
    private static final HttpStatus BASE_HTTP_STATUS = HttpStatus.NOT_FOUND;

    public EntityNotFoundException(@NotNull String message) {
        super(message, BASE_HTTP_STATUS);
    }

    public EntityNotFoundException(@NotNull Class<?> clazz, @NotNull Long id) {
        this("%s %s %s".formatted(clazz.getSimpleName(), BASE_MESSAGE, id.toString()));
    }

    public static Supplier<EntityNotFoundException> error(String message) {
        return () -> new EntityNotFoundException(message);
    }

    public static Supplier<EntityNotFoundException> error(Class<?> classType, Long id) {
        return () -> new EntityNotFoundException("%s %s %s".formatted(classType.getSimpleName(), BASE_MESSAGE, id.toString()));
    }
}
