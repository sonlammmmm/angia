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

@Component // Filter rate limit cho toàn bộ HTTP request
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService; // Service xử lý logic rate limit
    private final RateLimitProperties properties; // Cấu hình rate limit
    private final ObjectMapper objectMapper; // Serialize JSON response khi bị chặn

    public RateLimitFilter(RateLimitService rateLimitService, RateLimitProperties properties, ObjectMapper objectMapper) { // Constructor inject
        this.rateLimitService = rateLimitService; // Gán service
        this.properties = properties; // Gán cấu hình
        this.objectMapper = objectMapper; // Gán ObjectMapper
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.isEnabled()) { // Nếu tắt rate limit thì cho qua
            filterChain.doFilter(request, response); // Tiếp tục filter chain
            return; // Kết thúc filter
        }

        String key = resolveRateLimitKey(request); // Xác định key theo user/IP
        RateLimitResult result = rateLimitService.checkAndIncrement(key); // Kiểm tra & tăng bộ đếm

        response.setHeader("X-RateLimit-Limit", String.valueOf(properties.getMaxRequests())); // Tổng số request tối đa
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining())); // Số request còn lại
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.resetAtEpochSeconds())); // Mốc reset cửa sổ

        if (!result.allowed()) { // Nếu vượt limit
            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds())); // Thời gian cần chờ
            response.setContentType(MediaType.APPLICATION_JSON_VALUE); // Trả JSON

            Map<String, Object> body = new HashMap<>(); // Body response
            body.put("status", "error"); // Trạng thái lỗi
            body.put("message", "Rate limit exceeded. Try again in 1 hour."); // Thông báo
            body.put("retryAfterSeconds", result.retryAfterSeconds()); // Số giây cần chờ
            body.put("resetAt", Instant.ofEpochSecond(result.resetAtEpochSeconds()).toString()); // Thời điểm reset

            objectMapper.writeValue(response.getOutputStream(), body); // Ghi JSON ra output
            return; // Kết thúc filter khi bị chặn
        }

        filterChain.doFilter(request, response); // Cho phép request đi tiếp
    }

    private String resolveRateLimitKey(HttpServletRequest request) { // Xác định key theo user/IP
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Lấy auth hiện tại
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) { // Nếu đã đăng nhập
            return "user:" + authentication.getName(); // Key theo username
        }

        String forwardedFor = request.getHeader("X-Forwarded-For"); // Lấy IP proxy nếu có
        if (forwardedFor != null && !forwardedFor.isBlank()) { // Nếu có header
            return "ip:" + forwardedFor.split(",")[0].trim(); // Lấy IP đầu tiên
        }

        return "ip:" + request.getRemoteAddr(); // Fallback dùng IP trực tiếp
    }
}
