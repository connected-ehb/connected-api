package com.ehb.connected.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "connected")
public record ConnectedProperties(
        String canvasUri,
        String frontendUri
) {}
