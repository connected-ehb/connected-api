package com.ehb.connected.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Configuration for Spring Session with Redis using JSON serialization.
 * This prevents serialization issues with JPA entities that don't implement Serializable.
 */
@Configuration
@EnableRedisHttpSession
public class SessionConfig {

    /**
     * Configure Redis serializer to use JSON instead of Java serialization.
     * This is more flexible and doesn't require all session objects to implement Serializable.
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Register Hibernate6Module to handle lazy-loading and proxies properly
        Hibernate6Module hibernate6Module = new Hibernate6Module();
        // Don't serialize lazy-loaded properties that haven't been initialized
        hibernate6Module.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        // Serialize identifier for lazy-loaded entities instead of forcing load
        hibernate6Module.enable(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
        objectMapper.registerModule(hibernate6Module);

        // Register modules for Java 8 time types
        objectMapper.registerModule(new JavaTimeModule());

        // Register Spring Security Jackson modules for proper serialization of security types
        objectMapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));

        // Disable writing dates as timestamps
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Fail on self-references to prevent circular dependency issues
        objectMapper.disable(SerializationFeature.FAIL_ON_SELF_REFERENCES);

        // IMPORTANT: Ignore unknown properties during deserialization
        // This makes deployments backwards-compatible with old session data in Redis
        // Old sessions with extra properties (e.g., "name", "authorities") can still be read
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Configure polymorphic type validator to allow specific packages
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)
            .build();

        // Enable default typing for polymorphic types (required for Spring Security objects)
        objectMapper.activateDefaultTyping(
            ptv,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
