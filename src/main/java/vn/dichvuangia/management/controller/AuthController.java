package vn.dichvuangia.management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.dichvuangia.management.common.ApiResponse;
import vn.dichvuangia.management.dto.request.ChangePasswordRequest;
import vn.dichvuangia.management.dto.request.GoogleLoginRequest;
import vn.dichvuangia.management.dto.request.LoginRequest;
import vn.dichvuangia.management.dto.request.RegisterRequest;
import vn.dichvuangia.management.dto.response.AuthResponse;
import vn.dichvuangia.management.dto.response.TokenRefreshResponse;
import vn.dichvuangia.management.service.AuthService;

@Tag(name = "Auth", description = "Đăng ký, đăng nhập, làm mới token, đăng xuất")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE = "refreshToken";
    private static final long COOKIE_MAX_AGE = 604800L; // 7 ngày (giây)

    private final AuthService authService;

    // ── POST /auth/register ────────────────────────────────────────────────────

    @Operation(summary = "Đăng ký tài khoản khách hàng — tự động đăng nhập sau khi đăng ký")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Đăng ký thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc trùng username/phone")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse authResponse = authService.register(request);

        ResponseCookie cookie = buildRefreshCookie(authResponse.getRefreshToken(), COOKIE_MAX_AGE);

        return ResponseEntity.status(201)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Đăng ký thành công", authResponse));
    }

    // ── POST /auth/login ───────────────────────────────────────────────────────

    @Operation(summary = "Đăng nhập")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Tài khoản bị khóa")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);

        ResponseCookie cookie = buildRefreshCookie(authResponse.getRefreshToken(), COOKIE_MAX_AGE);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Đăng nhập thành công", authResponse));
    }

    // ── POST /auth/google ──────────────────────────────────────────────────────

    @Operation(summary = "Đăng nhập bằng Google — gửi ID token từ Google Sign-In")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập Google thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Google ID token không hợp lệ")
    })
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {

        AuthResponse authResponse = authService.googleLogin(request);

        ResponseCookie cookie = buildRefreshCookie(authResponse.getRefreshToken(), COOKIE_MAX_AGE);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Đăng nhập Google thành công", authResponse));
    }

    // ── POST /auth/refresh ─────────────────────────────────────────────────────

    @Operation(summary = "Làm mới Access Token bằng Refresh Token trong Cookie")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cấp token mới thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ hoặc hết hạn")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Không tìm thấy refresh token", "UNAUTHORIZED"));
        }

        TokenRefreshResponse result = authService.refresh(refreshToken);

        ResponseCookie cookie = buildRefreshCookie(result.getNewRefreshToken(), COOKIE_MAX_AGE);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Cấp token mới thành công", result));
    }

    // ── POST /auth/logout ──────────────────────────────────────────────────────

    @Operation(summary = "Đăng xuất — xóa Refresh Token")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng xuất thành công")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {

        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }

        // Clear cookie phía client (maxAge = 0)
        ResponseCookie clearCookie = buildRefreshCookie("", 0L);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(ApiResponse.success("Đăng xuất thành công", null));
    }

    // ── PUT /auth/change-password ──────────────────────────────────────────────

    @Operation(summary = "Đổi mật khẩu — yêu cầu xác thực")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Mật khẩu không hợp lệ")
    })
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }

    // ─── helper ────────────────────────────────────────────────────────────────

    private ResponseCookie buildRefreshCookie(String value, long maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(false)     // đổi thành true khi deploy HTTPS
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }
}
