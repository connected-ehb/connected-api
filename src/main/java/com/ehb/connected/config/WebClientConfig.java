package com.ehb.connected.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${custom.canvas-api-uri}")
    private String canvasApiUri;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(canvasApiUri)
                .build();
    }
}
