package com.ehb.connected.config;

import com.ehb.connected.domain.impl.auth.helpers.CustomAuthenticationSuccessHandler;
import com.ehb.connected.domain.impl.auth.helpers.CustomOAuth2UserService;
import com.ehb.connected.domain.impl.auth.helpers.TokenRefreshFilter;
import lombok.RequiredArgsConstructor;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final TokenRefreshFilter tokenRefreshFilter;
    private final CorsConfig corsConfig;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfig.corsFilter()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/logout", "/auth/register", "/login/**", "/login/oauth2/authorization/canvas", "/auth/logout", "/oauth2/authorization/canvas", "/error","/ws/**", "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .defaultSuccessUrl("http://localhost:4200", true)
                        .failureUrl("http://localhost:4200/login")
                )
                .formLogin(form -> form
                        .loginPage("http://localhost:4200/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("http://localhost:4200", true)
                        .successHandler(customAuthenticationSuccessHandler)
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        // Ensure API requests return 401 instead of redirecting
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .addFilterBefore(tokenRefreshFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessHandler(customLogoutSuccessHandler)
                        .permitAll()
                );
        return httpSecurity.build();
    }
}
