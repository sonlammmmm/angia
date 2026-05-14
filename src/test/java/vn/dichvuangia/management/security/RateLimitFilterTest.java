package vn.dichvuangia.management.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    @Test
    @DisplayName("Login endpoint uses strict 60-second policy and dynamic retry message")
    void loginPolicy_appliesAndReturnsDynamicRetryMessage() throws Exception {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setMaxRequests(100);
        properties.setWindowSeconds(3600);

        RateLimitService service = new RateLimitService(properties);
        RateLimitFilter filter = new RateLimitFilter(service, properties, new com.fasterxml.jackson.databind.ObjectMapper());

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        request.setServletPath("/auth/login");
        request.setRemoteAddr("10.0.0.7");

        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse allowedResponse = new MockHttpServletResponse();
            filter.doFilter(request, allowedResponse, new MockFilterChain());
            assertThat(allowedResponse.getStatus()).isNotEqualTo(429);
        }

        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        filter.doFilter(request, blockedResponse, new MockFilterChain());

        assertThat(blockedResponse.getStatus()).isEqualTo(429);
        assertThat(blockedResponse.getHeader("Retry-After")).isNotBlank();
        assertThat(blockedResponse.getContentAsString()).contains("Try again in").contains("seconds");
    }

    @Test
    @DisplayName("Do not trust X-Forwarded-For when trust flag is disabled")
    void forwardedFor_ignoredWhenNotTrusted() throws Exception {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setTrustForwardedFor(false);
        properties.setMaxRequests(1);
        properties.setWindowSeconds(3600);

        RateLimitService service = new RateLimitService(properties);
        RateLimitFilter filter = new RateLimitFilter(service, properties, new com.fasterxml.jackson.databind.ObjectMapper());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/products");
        request.setServletPath("/products");
        request.setRemoteAddr("192.168.1.10");
        request.addHeader("X-Forwarded-For", "8.8.8.8");

        MockHttpServletResponse first = new MockHttpServletResponse();
        filter.doFilter(request, first, new MockFilterChain());
        assertThat(first.getStatus()).isNotEqualTo(429);

        MockHttpServletResponse second = new MockHttpServletResponse();
        filter.doFilter(request, second, new MockFilterChain());
        assertThat(second.getStatus()).isEqualTo(429);
    }
}
