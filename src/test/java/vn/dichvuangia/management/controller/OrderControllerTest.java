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
import vn.dichvuangia.management.common.enums.OrderStatus;
import vn.dichvuangia.management.dto.request.OrderCreateRequest;
import vn.dichvuangia.management.dto.request.OrderItemRequest;
import vn.dichvuangia.management.dto.request.OrderStatusUpdateRequest;
import vn.dichvuangia.management.dto.response.OrderResponse;
import vn.dichvuangia.management.security.SecurityConfig;
import vn.dichvuangia.management.security.UserDetailsServiceImpl;
import vn.dichvuangia.management.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private OrderResponse sampleOrder() {
        return OrderResponse.builder()
                .id(1L)
                .orderCode("ORD-001")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("5000000"))
                .shippingAddress("123 Đường ABC, HCM")
                .customerId(1L)
                .customerName("Nguyen Van A")
                .customerPhone("0901234567")
                .createdAt(LocalDateTime.now())
                .items(List.of())
                .build();
    }

    // ── GET /orders ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /orders - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void getAll_asAdmin_returns200() throws Exception {
        when(orderService.getAll(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleOrder())));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].orderCode").value("ORD-001"));
    }

    @Test
    @DisplayName("GET /orders?status=PENDING - SALE → 200")
    @WithMockUser(roles = "SALE")
    void getAll_withStatusFilter_returns200() throws Exception {
        when(orderService.getAll(eq(OrderStatus.PENDING), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleOrder())));

        mockMvc.perform(get("/orders").param("status", "PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /orders - không auth → 401")
    void getAll_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /orders/{id} ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /orders/1 - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void getById_asAdmin_returns200() throws Exception {
        when(orderService.getById(1L)).thenReturn(sampleOrder());

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderCode").value("ORD-001"));
    }

    // ── POST /orders ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /orders - SALE → 201")
    @WithMockUser(roles = "SALE")
    void create_asSale_returns201() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setCustomerId(1L);
        req.setItems(List.of(item));
        req.setShippingAddress("123 Đường ABC, HCM");

        when(orderService.create(any())).thenReturn(sampleOrder());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Tạo đơn hàng thành công"))
                .andExpect(jsonPath("$.data.orderCode").value("ORD-001"));
    }

    @Test
    @DisplayName("POST /orders - thiếu items → 400")
    @WithMockUser(roles = "SALE")
    void create_missingItems_returns400() throws Exception {
        OrderCreateRequest req = new OrderCreateRequest();
        req.setCustomerId(1L);
        req.setShippingAddress("123 Đường ABC");
        // items bị thiếu

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /orders - không auth → 401")
    void create_noAuth_returns401() throws Exception {
        OrderCreateRequest req = new OrderCreateRequest();
        req.setCustomerId(1L);
        req.setShippingAddress("Test");
        req.setItems(List.of());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── PATCH /orders/{id}/status ─────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /orders/1/status - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void updateStatus_asAdmin_returns200() throws Exception {
        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest();
        req.setStatus(OrderStatus.PROCESSING);

        when(orderService.updateStatus(eq(1L), any())).thenReturn(sampleOrder());

        mockMvc.perform(patch("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật trạng thái thành công"));
    }

    @Test
    @DisplayName("PATCH /orders/1/status - MANAGEMENT → 200")
    @WithMockUser(roles = "MANAGEMENT")
    void updateStatus_asManagement_returns200() throws Exception {
        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest();
        req.setStatus(OrderStatus.PROCESSING);

        when(orderService.updateStatus(eq(1L), any())).thenReturn(sampleOrder());

        mockMvc.perform(patch("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
