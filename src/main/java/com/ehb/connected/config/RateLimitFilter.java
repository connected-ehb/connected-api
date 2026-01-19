package com.ehb.connected.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple rate limiting filter using Bucket4j.
 * Different limits for auth endpoints vs regular API.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip rate limiting for health checks and static resources
        if (shouldSkipRateLimit(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = getClientIdentifier(request);
        String key = clientId + ":" + getRateLimitKey(path);

        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(path));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitSeconds));
            response.setContentType("application/json");
            response.getWriter().write(
                    String.format("{\"error\":\"Too many requests. Try again in %d seconds.\"}", waitSeconds)
            );
        }
    }

    private Bucket createBucket(String path) {
        // Auth endpoints: 5 requests/minute (brute force protection)
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")) {
            return Bucket.builder()
                    .addLimit(limit -> limit.capacity(5).refillGreedy(5, Duration.ofMinutes(1)))
                    .build();
        }

        // Other auth endpoints: 20 requests/minute
        if (path.startsWith("/api/auth/") || path.startsWith("/api/users/verify")) {
            return Bucket.builder()
                    .addLimit(limit -> limit.capacity(20).refillGreedy(20, Duration.ofMinutes(1)))
                    .build();
        }

        // Standard API: 150 requests/minute
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(150).refillGreedy(150, Duration.ofMinutes(1)))
                .build();
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isEmpty())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }

    private String getRateLimitKey(String path) {
        if (path.startsWith("/api/auth/login")) return "auth:login";
        if (path.startsWith("/api/auth/register")) return "auth:register";
        if (path.startsWith("/api/auth/")) return "auth:other";
        return "api:standard";
    }

    private boolean shouldSkipRateLimit(String path) {
        return path.startsWith("/actuator/health") ||
               path.startsWith("/error") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".ico");
    }
}
