package com.ehb.connected.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that adds the CSRF token to the response headers.
 * This allows SPAs (Single Page Applications) to read the token from the response header
 * in cross-origin scenarios where cookies cannot be read by JavaScript.
 *
 * The token is sent in the 'X-XSRF-TOKEN' header and can be cached by the frontend
 * for subsequent state-changing requests.
 */
@Component
public class CsrfTokenResponseHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Load the CSRF token (this triggers Spring Security to generate it if needed)
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        // If token exists, add it to the response header
        if (csrfToken != null) {
            response.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());
        }

        filterChain.doFilter(request, response);
    }
}
