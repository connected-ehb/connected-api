package com.ehb.connected.config;

import com.ehb.connected.domain.impl.auth.helpers.CustomAuthenticationSuccessHandler;
import com.ehb.connected.domain.impl.auth.helpers.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final CorsConfig corsConfig;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Value("${connected.frontend-uri}")
    private String frontendUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfig.corsFilter()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/login/**", 
                                "/login/oauth2/authorization/canvas", 
                                "/oauth2/authorization/canvas", 
                                "/error",
                                "/ws/**", 
                                "/actuator/**",
                                "/api/users/verify" // Email verification endpoint
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(customAuthenticationSuccessHandler)
                        .defaultSuccessUrl(frontendUri, true)
                        .failureUrl(frontendUri + "/login?error=oauth2")
                )
                .formLogin(form -> form
                        .loginPage(frontendUri + "/login")
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(customAuthenticationSuccessHandler)
                        .defaultSuccessUrl(frontendUri, true)
                        .failureUrl(frontendUri + "/login?error=form")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .logoutSuccessUrl(frontendUri + "/login?logout=success")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return httpSecurity.build();
    }
}
