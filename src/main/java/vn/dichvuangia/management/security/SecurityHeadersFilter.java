package vn.dichvuangia.management.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ⚠️ SECURITY: Add HTTP Security Headers to prevent common web vulnerabilities
 * - HSTS: Enforce HTTPS
 * - CSP: Prevent XSS attacks
 * - X-Frame-Options: Prevent clickjacking
 * - X-Content-Type-Options: Prevent MIME sniffing
 * - X-XSS-Protection: Legacy XSS protection
 */
@Slf4j
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // HSTS: Enforce HTTPS for 1 year
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        
        // CSP: Strict Content Security Policy to prevent XSS
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self'; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'; " +
            "base-uri 'self'; " +
            "form-action 'self'");
        
        // X-Frame-Options: Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");
        
        // X-Content-Type-Options: Prevent MIME sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // X-XSS-Protection: Legacy XSS protection (for older browsers)
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Referrer-Policy: Control referrer information
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions-Policy: Control browser features
        response.setHeader("Permissions-Policy", "accelerometer=(), camera=(), microphone=(), geolocation=()");
        
        filterChain.doFilter(request, response);
    }
}
