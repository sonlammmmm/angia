package vn.dichvuangia.management.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/auth/login";
    private static final String REGISTER_PATH = "/auth/register";
    private static final String REFRESH_PATH = "/auth/refresh";

    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitService rateLimitService, RateLimitProperties properties, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveRateLimitKey(request);

        int maxRequests = properties.getMaxRequests();
        long windowSeconds = properties.getWindowSeconds();

        String path = request.getServletPath();
        if (LOGIN_PATH.equals(path)) {
            maxRequests = properties.getLoginMaxRequests();
            windowSeconds = properties.getLoginWindowSeconds();
        } else if (REGISTER_PATH.equals(path)) {
            maxRequests = properties.getRegisterMaxRequests();
            windowSeconds = properties.getRegisterWindowSeconds();
        } else if (REFRESH_PATH.equals(path)) {
            maxRequests = properties.getRefreshMaxRequests();
            windowSeconds = properties.getRefreshWindowSeconds();
        }

        RateLimitResult result = rateLimitService.checkAndIncrement(key, maxRequests, windowSeconds);

        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.resetAtEpochSeconds()));

        if (!result.allowed()) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> body = new HashMap<>();
            body.put("status", "error");
            body.put("message", "Rate limit exceeded. Try again in " + result.retryAfterSeconds() + " seconds.");
            body.put("retryAfterSeconds", result.retryAfterSeconds());
            body.put("resetAt", Instant.ofEpochSecond(result.resetAtEpochSeconds()).toString());

            objectMapper.writeValue(response.getOutputStream(), body);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveRateLimitKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "user:" + authentication.getName();
        }

        if (properties.isTrustForwardedFor()) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                String firstHop = forwardedFor.split(",")[0].trim();
                if (!firstHop.isBlank()) {
                    return "ip:" + firstHop;
                }
            }
        }

        return "ip:" + request.getRemoteAddr();
    }
}
