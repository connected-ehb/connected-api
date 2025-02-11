package com.ehb.connected.config;

import com.ehb.connected.domain.impl.auth.helpers.LoadOAuth2UserService;
import com.ehb.connected.domain.impl.auth.helpers.OAuth2LoginSuccessHandler;
import com.ehb.connected.domain.impl.auth.helpers.TokenRefreshFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler successHandler;
    private final TokenRefreshFilter tokenRefreshFilter;
    private final CorsConfig corsConfig;
    private final LoadOAuth2UserService loadOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfig.corsFilter()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login/**", "/login/oauth2/authorization/canvas", "/api/logout", "/oauth2/authorization/canvas", "/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(loadOAuth2UserService)
                        )
                        .successHandler(successHandler)
                )
                .exceptionHandling(exception -> exception
                        // Ensure API requests return 401 instead of redirecting
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .addFilterBefore(tokenRefreshFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }
}
