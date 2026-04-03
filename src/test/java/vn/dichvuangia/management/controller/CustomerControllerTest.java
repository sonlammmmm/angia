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
import vn.dichvuangia.management.dto.request.CustomerCreateRequest;
import vn.dichvuangia.management.dto.request.CustomerUpdateRequest;
import vn.dichvuangia.management.dto.response.CustomerResponse;
import vn.dichvuangia.management.security.SecurityConfig;
import vn.dichvuangia.management.security.UserDetailsServiceImpl;
import vn.dichvuangia.management.service.CustomerService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(SecurityConfig.class)
@DisplayName("CustomerController Tests")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private CustomerResponse sampleCustomer() {
        return CustomerResponse.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .phone("0901234567")
                .address("HCM")
                .createdAt(LocalDateTime.now())
                .createdById(2L)
                .createdByUsername("sale01")
                .build();
    }

    // ── GET /customers/me ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /customers/me - CUSTOMER → 200")
    @WithMockUser(roles = "CUSTOMER")
    void getMyProfile_asCustomer_returns200() throws Exception {
        when(customerService.getMyProfile()).thenReturn(sampleCustomer());

        mockMvc.perform(get("/customers/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van A"));
    }

    @Test
    @DisplayName("GET /customers/me - không auth → 401")
    void getMyProfile_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/customers/me"))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /customers/me ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /customers/me - CUSTOMER → 200")
    @WithMockUser(roles = "CUSTOMER")
    void updateMyProfile_asCustomer_returns200() throws Exception {
        CustomerUpdateRequest req = new CustomerUpdateRequest();
        req.setFullName("Nguyen Van B");
        req.setPhone("0901111111");

        when(customerService.updateMyProfile(any())).thenReturn(sampleCustomer());

        mockMvc.perform(put("/customers/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật thành công"));
    }

    @Test
    @DisplayName("PUT /customers/me - phone sai định dạng → 400")
    @WithMockUser(roles = "CUSTOMER")
    void updateMyProfile_invalidPhone_returns400() throws Exception {
        CustomerUpdateRequest req = new CustomerUpdateRequest();
        req.setPhone("12345"); // sai

        mockMvc.perform(put("/customers/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── GET /customers ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /customers - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void getAll_asAdmin_returns200() throws Exception {
        when(customerService.getAll(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleCustomer())));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fullName").value("Nguyen Van A"));
    }

    @Test
    @DisplayName("GET /customers - SALE → 200")
    @WithMockUser(roles = "SALE")
    void getAll_asSale_returns200() throws Exception {
        when(customerService.getAll(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleCustomer())));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /customers - không auth → 401")
    void getAll_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /customers/{id} ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /customers/1 - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void getById_asAdmin_returns200() throws Exception {
        when(customerService.getById(1L)).thenReturn(sampleCustomer());

        mockMvc.perform(get("/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ── POST /customers ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /customers - SALE → 201")
    @WithMockUser(roles = "SALE")
    void create_asSale_returns201() throws Exception {
        CustomerCreateRequest req = new CustomerCreateRequest();
        req.setFullName("Nguyen Van A");
        req.setPhone("0901234567");

        when(customerService.create(any())).thenReturn(sampleCustomer());

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.phone").value("0901234567"));
    }

    @Test
    @DisplayName("POST /customers - thiếu fullName → 400")
    @WithMockUser(roles = "SALE")
    void create_missingFullName_returns400() throws Exception {
        CustomerCreateRequest req = new CustomerCreateRequest();
        req.setPhone("0901234567");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /customers/{id} ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /customers/1 - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void update_asAdmin_returns200() throws Exception {
        CustomerUpdateRequest req = new CustomerUpdateRequest();
        req.setFullName("Updated Name");

        when(customerService.update(eq(1L), any())).thenReturn(sampleCustomer());

        mockMvc.perform(put("/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
