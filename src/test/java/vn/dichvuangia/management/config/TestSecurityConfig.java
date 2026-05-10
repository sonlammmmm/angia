package vn.dichvuangia.management.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import vn.dichvuangia.management.security.RateLimitProperties;
import vn.dichvuangia.management.security.RateLimitService;

/**
 * Cấu hình Security đơn giản cho @WebMvcTest.
 * Loại bỏ JWT, custom filters (RateLimitFilter, SecurityHeadersFilter),
 * chỉ giữ lại authorization rules để test controller security.
 *
 * Sử dụng @WithMockUser để giả lập authentication trong test.
 *
 * Mock beans: RateLimitService, RateLimitProperties vì @WebMvcTest
 * scan @Component filters.
 */
@TestConfiguration
public class TestSecurityConfig {

        @Bean
        public RateLimitService rateLimitService() {
                return Mockito.mock(RateLimitService.class);
        }

        @Bean
        public RateLimitProperties rateLimitProperties() {
                return Mockito.mock(RateLimitProperties.class);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .anonymous(AbstractHttpConfigurer::disable)
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.sendError(401, "Unauthorized");
                                                }))
                                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Public
                                                .requestMatchers("/auth/register", "/auth/login", "/auth/refresh",
                                                                "/auth/logout", "/auth/google")
                                                .permitAll()
                                                .requestMatchers("/auth/change-password").authenticated()
                                                .requestMatchers(HttpMethod.GET, "/products/**", "/brands/**",
                                                                "/services/**")
                                                .permitAll()

                                                // Guest checkout
                                                .requestMatchers("/guest/**").permitAll()

                                                // Admin + Management: quản lý tài khoản nhân viên
                                                .requestMatchers("/users/**").hasAnyRole("ADMIN", "MANAGEMENT")
                                                .requestMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")

                                                // Admin + Management: quản lý sản phẩm, thương hiệu, dịch vụ
                                                .requestMatchers(HttpMethod.POST, "/products/**", "/brands/**",
                                                                "/services/**")
                                                .hasAnyRole("ADMIN", "MANAGEMENT")
                                                .requestMatchers(HttpMethod.PUT, "/products/**", "/brands/**",
                                                                "/services/**")
                                                .hasAnyRole("ADMIN", "MANAGEMENT")

                                                // Quản lý khách hàng
                                                .requestMatchers(HttpMethod.GET, "/customers/me").authenticated()
                                                .requestMatchers(HttpMethod.PUT, "/customers/me").authenticated()
                                                .requestMatchers("/customers/**")
                                                .hasAnyRole("ADMIN", "MANAGEMENT", "SALE")

                                                // Booking: gán kỹ thuật viên (ADMIN, MANAGEMENT)
                                                .requestMatchers("/maintenance-bookings/*/assign")
                                                .hasAnyRole("ADMIN", "MANAGEMENT")

                                                // Booking: hoàn thành (ADMIN, TECHNICIAN)
                                                .requestMatchers("/maintenance-bookings/*/complete")
                                                .hasAnyRole("ADMIN", "TECHNICIAN")

                                                // Booking: hủy (ADMIN, MANAGEMENT, CUSTOMER)
                                                .requestMatchers("/maintenance-bookings/*/cancel")
                                                .hasAnyRole("ADMIN", "MANAGEMENT", "CUSTOMER")

                                                // Tất cả request khác cần xác thực
                                                .anyRequest().authenticated())
                                .build();
        }
}
