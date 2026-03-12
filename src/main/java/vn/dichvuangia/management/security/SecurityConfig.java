package vn.dichvuangia.management.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // ── Beans cốt lõi ──────────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(
                Base64.getDecoder().decode(jwtSecret), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKeySpec secretKey = new SecretKeySpec(
                Base64.getDecoder().decode(jwtSecret), "HmacSHA256");
        // Phải set algorithm HS256, nếu không NimbusJwtEncoder không chọn được key
        var jwk = new OctetSequenceKey.Builder(secretKey)
                .algorithm(com.nimbusds.jose.JWSAlgorithm.HS256)
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    @SuppressWarnings("deprecation") // Spring Security 6.5: constructor/setter deprecated — dùng tạm cho đồ án
    public AuthenticationManager authenticationManager() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    // ── JWT Authority Converter ──────────────────────────────────────────────
    // Mặc định Spring thêm prefix "SCOPE_" khi đọc claim "scope".
    // Vì JWT đã chứa "ROLE_ADMIN", ta bỏ prefix để hasRole("ADMIN") match đúng.

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // bỏ prefix "SCOPE_"

        var jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtConverter;
    }

    // ── Security Filter Chain ──────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**", "/brands/**", "/services/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/files/**").permitAll()

                        // Admin only
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")

                        // Admin + Management: quản lý sản phẩm, thương hiệu, dịch vụ
                        .requestMatchers(HttpMethod.POST, "/products/**", "/brands/**", "/services/**")
                                .hasAnyRole("ADMIN", "MANAGEMENT")
                        .requestMatchers(HttpMethod.PUT, "/products/**", "/brands/**", "/services/**")
                                .hasAnyRole("ADMIN", "MANAGEMENT")

                        // Quản lý khách hàng: ADMIN, MANAGEMENT, SALE (không cho CUSTOMER xem danh sách)
                        .requestMatchers(HttpMethod.GET, "/customers/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/customers/me").authenticated()
                        .requestMatchers("/customers/**").hasAnyRole("ADMIN", "MANAGEMENT", "SALE")

                        // Booking: gán kỹ thuật viên (ADMIN, MANAGEMENT)
                        .requestMatchers("/maintenance-bookings/*/assign")
                                .hasAnyRole("ADMIN", "MANAGEMENT")

                        // Booking: hoàn thành (ADMIN, TECHNICIAN)
                        .requestMatchers("/maintenance-bookings/*/complete")
                                .hasAnyRole("ADMIN", "TECHNICIAN")

                        // Booking: hủy (ADMIN, MANAGEMENT)
                        .requestMatchers("/maintenance-bookings/*/cancel")
                                .hasAnyRole("ADMIN", "MANAGEMENT")

                        // Tạo đơn hàng & đặt lịch bảo trì: mọi user đã xác thực (bao gồm CUSTOMER)
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))
                .build();
    }
}
