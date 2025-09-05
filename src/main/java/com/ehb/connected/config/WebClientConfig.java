package com.ehb.connected.config;

import com.ehb.connected.domain.impl.auth.helpers.CanvasApiInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${custom.canvas-api-uri}")
    private String canvasApiUri;

    @Bean
    public WebClient webClient(WebClient.Builder builder, CanvasApiInterceptor canvasApiInterceptor) {
        return builder.baseUrl(canvasApiUri)
                .filter(canvasApiInterceptor.tokenRefreshFilter())
                .build();
    }
}
