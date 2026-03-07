package vn.dichvuangia.management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.dto.request.LoginRequest;
import vn.dichvuangia.management.dto.request.RegisterRequest;
import vn.dichvuangia.management.dto.response.AuthResponse;
import vn.dichvuangia.management.dto.response.TokenRefreshResponse;
import vn.dichvuangia.management.entity.Customer;
import vn.dichvuangia.management.entity.RefreshToken;
import vn.dichvuangia.management.entity.Role;
import vn.dichvuangia.management.entity.User;
import vn.dichvuangia.management.exception.ResourceNotFoundException;
import vn.dichvuangia.management.repository.CustomerRepository;
import vn.dichvuangia.management.repository.RefreshTokenRepository;
import vn.dichvuangia.management.repository.RoleRepository;
import vn.dichvuangia.management.repository.UserRepository;
import vn.dichvuangia.management.security.JwtService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;

    @Value("${app.jwt.expiration:3600}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration:604800}")
    private long refreshExpiration;

    /**
     * Đăng ký tài khoản khách hàng.
     * 1. Tạo User với role CUSTOMER
     * 2. Tạo Customer profile (createdBy = chính user vừa tạo)
     * 3. Tự động đăng nhập: trả về accessToken + refreshToken
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra username trùng
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
        }

        // Kiểm tra phone trùng
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại '" + request.getPhone() + "' đã được đăng ký");
        }

        // Lấy role CUSTOMER
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER chưa được khởi tạo"));

        // Tạo User
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(customerRole);
        user.setIsActive(true);
        user = userRepository.save(user);

        // Tạo Customer profile — createdBy = chính user vừa tạo
        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCreatedBy(user);
        customerRepository.save(customer);

        // Auto-login: cấp token ngay
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Đăng nhập — trả về accessToken (body) + refreshToken (giao Controller set Cookie).
     * Controller sẽ nhận refreshToken từ AuthResponse rồi set HttpOnly Cookie.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Spring Security đã xác thực xong, lấy User entity từ principal
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User không tìm thấy với username: " + authentication.getName()));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Refresh — Rotation: xóa token cũ, tạo cặp token mới.
     * Trả accessToken mới + refreshToken mới (giao Controller update Cookie).
     */
    @Transactional
    public TokenRefreshResponse refresh(String rawToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token không hợp lệ hoặc không tồn tại"));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new IllegalStateException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }

        User user = stored.getUser();

        // Rotation: xóa token cũ trước khi cấp mới
        refreshTokenRepository.delete(stored);
        refreshTokenRepository.flush();

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = createRefreshToken(user);

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .newRefreshToken(newRefreshToken)
                .build();
    }

    /**
     * Đăng xuất — xóa refresh token khỏi DB (Controller clear Cookie).
     */
    @Transactional
    public void logout(String rawToken) {
        refreshTokenRepository.findByToken(rawToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    private String createRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setToken(tokenValue);
        entity.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration));

        refreshTokenRepository.save(entity);
        return tokenValue;
    }
}
