package com.ehb.connected.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${connected.canvas-uri}")
    private String canvasApiUri;

    @Bean
    @Primary
    public WebClient webClient(
            WebClient.Builder builder,
            OAuth2AuthorizedClientManager authorizedClientManager) {

        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        // Automatically use the "canvas" registration for all requests
        oauth2Client.setDefaultClientRegistrationId("canvas");

        return builder
                .baseUrl(canvasApiUri)
                .filter(oauth2Client)
                .build();
    }
}

