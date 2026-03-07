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
import vn.dichvuangia.management.dto.request.BrandCreateRequest;
import vn.dichvuangia.management.dto.request.BrandUpdateRequest;
import vn.dichvuangia.management.dto.response.BrandResponse;
import vn.dichvuangia.management.security.SecurityConfig;
import vn.dichvuangia.management.security.UserDetailsServiceImpl;
import vn.dichvuangia.management.service.BrandService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BrandController.class)
@Import(SecurityConfig.class)
@DisplayName("BrandController Tests")
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BrandService brandService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private BrandResponse sampleBrand() {
        return BrandResponse.builder()
                .id(1L)
                .name("Kangaroo")
                .description("Thương hiệu Việt")
                .logoUrl("http://logo.png")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── GET /brands ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /brands - public, không cần auth → 200")
    void getAll_public_returns200() throws Exception {
        when(brandService.getAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleBrand())));

        mockMvc.perform(get("/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].name").value("Kangaroo"));
    }

    // ── GET /brands/{id} ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /brands/1 - public → 200")
    void getById_public_returns200() throws Exception {
        when(brandService.getById(1L)).thenReturn(sampleBrand());

        mockMvc.perform(get("/brands/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Kangaroo"));
    }

    // ── POST /brands ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /brands - ADMIN → 201")
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns201() throws Exception {
        BrandCreateRequest req = new BrandCreateRequest();
        req.setName("Kangaroo");
        req.setDescription("Thương hiệu Việt");

        when(brandService.create(any())).thenReturn(sampleBrand());

        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Kangaroo"));
    }

    @Test
    @DisplayName("POST /brands - MANAGEMENT → 201")
    @WithMockUser(roles = "MANAGEMENT")
    void create_asManagement_returns201() throws Exception {
        BrandCreateRequest req = new BrandCreateRequest();
        req.setName("Kangaroo");

        when(brandService.create(any())).thenReturn(sampleBrand());

        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /brands - không auth → 401")
    void create_noAuth_returns401() throws Exception {
        BrandCreateRequest req = new BrandCreateRequest();
        req.setName("Kangaroo");

        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /brands - thiếu name → 400")
    @WithMockUser(roles = "ADMIN")
    void create_missingName_returns400() throws Exception {
        BrandCreateRequest req = new BrandCreateRequest();
        // name bị thiếu

        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /brands/{id} ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /brands/1 - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void update_asAdmin_returns200() throws Exception {
        BrandUpdateRequest req = new BrandUpdateRequest();
        req.setName("Updated Name");

        when(brandService.update(eq(1L), any())).thenReturn(sampleBrand());

        mockMvc.perform(put("/brands/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    // ── DELETE /brands/{id} ──────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /brands/1 - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void delete_asAdmin_returns200() throws Exception {
        doNothing().when(brandService).softDelete(1L);

        mockMvc.perform(delete("/brands/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đã xóa thương hiệu"));
    }

    @Test
    @DisplayName("DELETE /brands/1 - SALE → 403")
    @WithMockUser(roles = "SALE")
    void delete_asSale_returns403() throws Exception {
        mockMvc.perform(delete("/brands/1"))
                .andExpect(status().isForbidden());
    }
}
