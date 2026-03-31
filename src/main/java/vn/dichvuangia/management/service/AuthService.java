package vn.dichvuangia.management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import vn.dichvuangia.management.dto.request.ChangePasswordRequest;
import vn.dichvuangia.management.dto.request.GoogleLoginRequest;
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
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@Slf4j
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

    @Value("${app.google.client-id:}")
    private String googleClientId;

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
     * Đăng nhập bằng Google — verify ID token, tạo/tìm user rồi cấp JWT.
     * Nếu user chưa tồn tại → tạo mới với role CUSTOMER + Customer profile.
     */
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        // 1. Verify Google ID token
        GoogleIdToken.Payload googlePayload = verifyGoogleToken(request.getCredential());

        String email = googlePayload.getEmail();
        String fullName = (String) googlePayload.get("name");
        if (fullName == null || fullName.isBlank()) {
            fullName = email.split("@")[0];
        }

        // 2. Tìm user theo email, nếu chưa có → tạo mới
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Tạo User mới với role CUSTOMER
            Role customerRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER chưa được khởi tạo"));

            user = new User();
            user.setUsername(email); // dùng email làm username
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString())); // random password
            user.setRole(customerRole);
            user.setIsActive(true);
            user = userRepository.save(user);

            // Tạo Customer profile
            Customer customer = new Customer();
            customer.setFullName(fullName);
            customer.setCreatedBy(user);
            customerRepository.save(customer);
        }

        // 3. Cấp token
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
     * Verify Google ID Token bằng google-api-client.
     */
    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            log.debug("Verifying Google ID token, clientId={}", googleClientId);
            log.debug("ID token (first 50 chars): {}", idTokenString.substring(0, Math.min(50, idTokenString.length())));

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.error("GoogleIdTokenVerifier.verify() returned null — token invalid or audience mismatch. Expected audience: {}", googleClientId);
                throw new IllegalArgumentException("Google ID token không hợp lệ");
            }
            log.debug("Google token verified successfully, email={}", idToken.getPayload().getEmail());
            return idToken.getPayload();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification exception: ", e);
            throw new IllegalArgumentException("Không thể xác thực Google ID token: " + e.getMessage());
        }
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

    /**
     * Đổi mật khẩu — xác nhận mật khẩu hiện tại, cập nhật mật khẩu mới.
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // Validate mật khẩu mới khớp xác nhận
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        // Lấy userId từ JWT
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Objects.requireNonNull(jwt.getClaim("userId"), "userId claim is missing from JWT");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        // Kiểm tra mật khẩu mới khác mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
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
