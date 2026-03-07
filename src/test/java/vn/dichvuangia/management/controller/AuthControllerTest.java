package vn.dichvuangia.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.dichvuangia.management.dto.request.LoginRequest;
import vn.dichvuangia.management.dto.request.RegisterRequest;
import vn.dichvuangia.management.dto.response.AuthResponse;
import vn.dichvuangia.management.security.SecurityConfig;
import vn.dichvuangia.management.security.UserDetailsServiceImpl;
import vn.dichvuangia.management.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private AuthResponse sampleAuthResponse() {
        return AuthResponse.builder()
                .accessToken("dummy-access-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .refreshToken("dummy-refresh-token")
                .build();
    }

    // ── /auth/register ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/register - thành công → 201")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("password123");
        req.setFullName("Nguyen Van A");
        req.setPhone("0901234567");

        when(authService.register(any())).thenReturn(sampleAuthResponse());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Đăng ký thành công"))
                .andExpect(jsonPath("$.data.accessToken").value("dummy-access-token"));
    }

    @Test
    @DisplayName("POST /auth/register - thiếu username → 400")
    void register_missingUsername_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setPassword("password123");
        req.setFullName("Nguyen Van A");
        req.setPhone("0901234567");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register - phone không hợp lệ → 400")
    void register_invalidPhone_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("password123");
        req.setFullName("Nguyen Van A");
        req.setPhone("12345"); // sai định dạng

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── /auth/login ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login - đúng thông tin → 200")
    void login_validCredentials_returns200() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("password");

        when(authService.login(any())).thenReturn(sampleAuthResponse());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.data.accessToken").value("dummy-access-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /auth/login - thiếu password → 400")
    void login_missingPassword_returns400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        // password bị thiếu

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
