package vn.dichvuangia.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.dichvuangia.management.dto.request.UserCreateRequest;
import vn.dichvuangia.management.dto.request.UserUpdateRequest;
import vn.dichvuangia.management.dto.response.UserResponse;
import vn.dichvuangia.management.security.SecurityConfig;
import vn.dichvuangia.management.security.UserDetailsServiceImpl;
import vn.dichvuangia.management.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private UserResponse sampleUser() {
        return UserResponse.builder()
                .id(1L)
                .username("sale01")
                .roleName("SALE")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── GET /users ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /users - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void getAll_asAdmin_returns200() throws Exception {
        when(userService.getAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleUser())));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].username").value("sale01"));
    }

    @Test
    @DisplayName("GET /users - MANAGEMENT → 200")
    @WithMockUser(roles = "MANAGEMENT")
    void getAll_asManagement_returns200() throws Exception {
    when(userService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleUser())));

    mockMvc.perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.data.content[0].username").value("sale01"));
    }

    @Test
    @DisplayName("GET /users - không auth → 401")
    void getAll_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /users/{id} ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /users/1 - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void getById_asAdmin_returns200() throws Exception {
        when(userService.getById(1L)).thenReturn(sampleUser());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ── POST /users ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /users - ADMIN → 201")
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns201() throws Exception {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("newstaff");
        req.setPassword("pass1234");
        req.setRoleId(2L);

        when(userService.create(any())).thenReturn(sampleUser());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Tạo tài khoản thành công"));
    }

    @Test
    @DisplayName("POST /users - SALE → 403")
    @WithMockUser(roles = "SALE")
    void create_asSale_returns403() throws Exception {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("newstaff");
        req.setPassword("pass1234");
        req.setRoleId(2L);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /users - thiếu username → 400")
    @WithMockUser(roles = "ADMIN")
    void create_missingUsername_returns400() throws Exception {
        UserCreateRequest req = new UserCreateRequest();
        req.setPassword("pass1234");
        req.setRoleId(2L);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /users/{id} ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /users/1 - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void update_asAdmin_returns200() throws Exception {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setPassword("newpassword");

        when(userService.update(eq(1L), any())).thenReturn(sampleUser());

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── PATCH /users/{id}/lock ──────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /users/1/lock - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void lock_asAdmin_returns200() throws Exception {
        doNothing().when(userService).lock(1L);

        mockMvc.perform(patch("/users/1/lock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đã khóa tài khoản"));
    }

    // ── PATCH /users/{id}/unlock ─────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /users/1/unlock - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void unlock_asAdmin_returns200() throws Exception {
        doNothing().when(userService).unlock(1L);

        mockMvc.perform(patch("/users/1/unlock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đã mở khóa tài khoản"));
    }
}
