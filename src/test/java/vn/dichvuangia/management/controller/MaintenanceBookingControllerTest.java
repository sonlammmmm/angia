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
import vn.dichvuangia.management.common.enums.BookingStatus;
import vn.dichvuangia.management.dto.request.BookingAssignRequest;
import vn.dichvuangia.management.dto.request.BookingCompleteRequest;
import vn.dichvuangia.management.dto.request.BookingCreateRequest;
import vn.dichvuangia.management.dto.response.BookingResponse;
import vn.dichvuangia.management.security.SecurityConfig;
import vn.dichvuangia.management.security.UserDetailsServiceImpl;
import vn.dichvuangia.management.service.MaintenanceBookingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaintenanceBookingController.class)
@Import(SecurityConfig.class)
@DisplayName("MaintenanceBookingController Tests")
class MaintenanceBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MaintenanceBookingService bookingService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private BookingResponse sampleBooking() {
        return BookingResponse.builder()
                .id(1L)
                .bookingCode("BK-001")
                .status(BookingStatus.PENDING)
                .bookingDate(LocalDateTime.now().plusDays(1))
                .customerId(1L)
                .customerName("Nguyen Van A")
                .customerPhone("0901234567")
                .serviceId(1L)
                .serviceName("Bảo trì định kỳ")
                .serviceBasePrice(new BigDecimal("200000"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── GET /maintenance-bookings ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /maintenance-bookings - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void getAll_asAdmin_returns200() throws Exception {
        when(bookingService.getAll(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleBooking())));

        mockMvc.perform(get("/maintenance-bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].bookingCode").value("BK-001"));
    }

    @Test
    @DisplayName("GET /maintenance-bookings - TECHNICIAN → 200")
    @WithMockUser(roles = "TECHNICIAN")
    void getAll_asTechnician_returns200() throws Exception {
        when(bookingService.getAll(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleBooking())));

        mockMvc.perform(get("/maintenance-bookings"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /maintenance-bookings - không auth → 401")
    void getAll_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/maintenance-bookings"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /maintenance-bookings/{id} ───────────────────────────────────────────

    @Test
    @DisplayName("GET /maintenance-bookings/1 - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void getById_asAdmin_returns200() throws Exception {
        when(bookingService.getById(1L)).thenReturn(sampleBooking());

        mockMvc.perform(get("/maintenance-bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookingCode").value("BK-001"));
    }

    // ── POST /maintenance-bookings ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /maintenance-bookings - SALE → 201")
    @WithMockUser(roles = "SALE")
    void create_asSale_returns201() throws Exception {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setCustomerId(1L);
        req.setServiceId(1L);
        req.setBookingDate(LocalDateTime.now().plusDays(3));

        when(bookingService.create(any())).thenReturn(sampleBooking());

        mockMvc.perform(post("/maintenance-bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Tạo lịch thành công"))
                .andExpect(jsonPath("$.data.bookingCode").value("BK-001"));
    }

    @Test
    @DisplayName("POST /maintenance-bookings - ADMIN → 201")
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns201() throws Exception {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setCustomerId(1L);
        req.setServiceId(1L);
        req.setBookingDate(LocalDateTime.now().plusDays(3));

        when(bookingService.create(any())).thenReturn(sampleBooking());

        mockMvc.perform(post("/maintenance-bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /maintenance-bookings - thiếu customerId → 400")
    @WithMockUser(roles = "SALE")
    void create_missingCustomerId_returns400() throws Exception {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setServiceId(1L);
        req.setBookingDate(LocalDateTime.now().plusDays(3));
        // thiếu customerId

        mockMvc.perform(post("/maintenance-bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /maintenance-bookings - không auth → 401")
    void create_noAuth_returns401() throws Exception {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setCustomerId(1L);
        req.setServiceId(1L);
        req.setBookingDate(LocalDateTime.now().plusDays(3));

        mockMvc.perform(post("/maintenance-bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── PATCH /maintenance-bookings/{id}/assign ───────────────────────────────────

    @Test
    @DisplayName("PATCH /maintenance-bookings/1/assign - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void assign_asAdmin_returns200() throws Exception {
        BookingAssignRequest req = new BookingAssignRequest();
        req.setTechnicianId(5L);

        BookingResponse confirmedBooking = BookingResponse.builder()
                .id(1L)
                .bookingCode("BK-001")
                .status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDateTime.now().plusDays(1))
                .customerId(1L)
                .customerName("Nguyen Van A")
                .customerPhone("0901234567")
                .serviceId(1L)
                .serviceName("Bảo trì định kỳ")
                .serviceBasePrice(new BigDecimal("200000"))
                .technicianId(5L)
                .technicianUsername("tech01")
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingService.assignTechnician(eq(1L), any())).thenReturn(confirmedBooking);

        mockMvc.perform(patch("/maintenance-bookings/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Gán kỹ thuật viên thành công"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PATCH /maintenance-bookings/1/assign - MANAGEMENT → 200")
    @WithMockUser(roles = "MANAGEMENT")
    void assign_asManagement_returns200() throws Exception {
        BookingAssignRequest req = new BookingAssignRequest();
        req.setTechnicianId(5L);

        when(bookingService.assignTechnician(eq(1L), any())).thenReturn(sampleBooking());

        mockMvc.perform(patch("/maintenance-bookings/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── PATCH /maintenance-bookings/{id}/complete ─────────────────────────────────

    @Test
    @DisplayName("PATCH /maintenance-bookings/1/complete - TECHNICIAN → 200")
    @WithMockUser(roles = "TECHNICIAN")
    void complete_asTechnician_returns200() throws Exception {
        BookingCompleteRequest req = new BookingCompleteRequest();
        req.setNotes("Đã hoàn thành bảo trì");

        BookingResponse completedBooking = BookingResponse.builder()
                .id(1L)
                .bookingCode("BK-001")
                .status(BookingStatus.COMPLETED)
                .bookingDate(LocalDateTime.now().plusDays(1))
                .customerId(1L)
                .customerName("Nguyen Van A")
                .customerPhone("0901234567")
                .serviceId(1L)
                .serviceName("Bảo trì định kỳ")
                .serviceBasePrice(new BigDecimal("200000"))
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingService.complete(eq(1L), any())).thenReturn(completedBooking);

        mockMvc.perform(patch("/maintenance-bookings/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hoàn thành lịch bảo trì"));
    }
}
