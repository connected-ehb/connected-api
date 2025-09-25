package com.ehb.connected.config;

import com.ehb.connected.domain.impl.auth.helpers.CanvasApiInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${connected.canvas-uri}")
    private String canvasApiUri;

    // Primary WebClient used across the app with token-refresh filter applied
    @Bean
    @Primary
    public WebClient webClient(WebClient.Builder builder, @Lazy CanvasApiInterceptor canvasApiInterceptor) {
        return builder.baseUrl(canvasApiUri)
                .filter(canvasApiInterceptor.tokenRefreshFilter())
                .build();
    }

    // Dedicated WebClient for auth flows (no interceptor) to avoid circular deps
    @Bean(name = "canvasAuthWebClient")
    public WebClient canvasAuthWebClient(WebClient.Builder builder) {
        return builder.baseUrl(canvasApiUri).build();
    }
}

