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
import vn.dichvuangia.management.dto.request.ServiceCreateRequest;
import vn.dichvuangia.management.dto.request.ServiceUpdateRequest;
import vn.dichvuangia.management.dto.response.ServiceResponse;
import vn.dichvuangia.management.security.SecurityConfig;
import vn.dichvuangia.management.security.UserDetailsServiceImpl;
import vn.dichvuangia.management.service.ServiceService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceController.class)
@Import(SecurityConfig.class)
@DisplayName("ServiceController Tests")
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServiceService serviceService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private ServiceResponse sampleService() {
        return ServiceResponse.builder()
                .id(1L)
                .serviceCode("DV001")
                .name("Bảo trì định kỳ")
                .description("Dịch vụ bảo trì máy lọc nước")
                .basePrice(new BigDecimal("200000"))
                .durationMinutes(60)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── GET /services ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /services - public → 200")
    void getAll_public_returns200() throws Exception {
        when(serviceService.getAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleService())));

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].name").value("Bảo trì định kỳ"));
    }

    // ── GET /services/{id} ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /services/1 - public → 200")
    void getById_public_returns200() throws Exception {
        when(serviceService.getById(1L)).thenReturn(sampleService());

        mockMvc.perform(get("/services/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.serviceCode").value("DV001"));
    }

    // ── POST /services ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /services - ADMIN → 201")
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns201() throws Exception {
        ServiceCreateRequest req = new ServiceCreateRequest();
        req.setName("Bảo trì định kỳ");
        req.setBasePrice(new BigDecimal("200000"));
        req.setDurationMinutes(60);

        when(serviceService.create(any())).thenReturn(sampleService());

        mockMvc.perform(post("/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Bảo trì định kỳ"));
    }

    @Test
    @DisplayName("POST /services - MANAGEMENT → 201")
    @WithMockUser(roles = "MANAGEMENT")
    void create_asManagement_returns201() throws Exception {
        ServiceCreateRequest req = new ServiceCreateRequest();
        req.setName("Vệ sinh lõi lọc");
        req.setBasePrice(new BigDecimal("100000"));

        when(serviceService.create(any())).thenReturn(sampleService());

        mockMvc.perform(post("/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /services - không auth → 401")
    void create_noAuth_returns401() throws Exception {
        ServiceCreateRequest req = new ServiceCreateRequest();
        req.setName("Test");
        req.setBasePrice(new BigDecimal("100000"));

        mockMvc.perform(post("/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /services - thiếu name → 400")
    @WithMockUser(roles = "ADMIN")
    void create_missingName_returns400() throws Exception {
        ServiceCreateRequest req = new ServiceCreateRequest();
        req.setBasePrice(new BigDecimal("200000"));

        mockMvc.perform(post("/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /services/{id} ───────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /services/1 - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void update_asAdmin_returns200() throws Exception {
        ServiceUpdateRequest req = new ServiceUpdateRequest();
        req.setName("Cập nhật dịch vụ");

        when(serviceService.update(eq(1L), any())).thenReturn(sampleService());

        mockMvc.perform(put("/services/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── DELETE /services/{id} ────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /services/1 - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void delete_asAdmin_returns200() throws Exception {
        doNothing().when(serviceService).softDelete(1L);

        mockMvc.perform(delete("/services/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đã xóa dịch vụ"));
    }

    @Test
    @DisplayName("DELETE /services/1 - SALE → 403")
    @WithMockUser(roles = "SALE")
    void delete_asSale_returns403() throws Exception {
        mockMvc.perform(delete("/services/1"))
                .andExpect(status().isForbidden());
    }
}
