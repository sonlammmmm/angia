package vn.dichvuangia.management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.dichvuangia.management.common.ApiResponse;
import vn.dichvuangia.management.common.enums.BookingStatus;
import vn.dichvuangia.management.dto.request.BookingAssignRequest;
import vn.dichvuangia.management.dto.request.BookingCompleteRequest;
import vn.dichvuangia.management.dto.request.BookingCreateRequest;
import vn.dichvuangia.management.dto.response.BookingResponse;
import vn.dichvuangia.management.service.MaintenanceBookingService;

import java.time.LocalDateTime;

@Tag(name = "Maintenance Bookings", description = "Quản lý lịch bảo trì")
@RestController
@RequestMapping("/maintenance-bookings")
@RequiredArgsConstructor
public class MaintenanceBookingController {

    private final MaintenanceBookingService bookingService;

    @Operation(summary = "Danh sách lịch bảo trì — TECHNICIAN chỉ thấy lịch của mình. Query: status, customerId, from, to")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAll(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getAll(status, from, to, customerId, pageable)));
    }

    @Operation(summary = "Chi tiết lịch bảo trì")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getById(id)));
    }

    @Operation(summary = "Tạo lịch bảo trì mới (status = PENDING) — ADMIN, MANAGEMENT, SALE")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khách hàng hoặc dịch vụ không tồn tại")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> create(
            @Valid @RequestBody BookingCreateRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Tạo lịch thành công", bookingService.create(request)));
    }

    @Operation(summary = "Gán kỹ thuật viên → CONFIRMED — ADMIN, MANAGEMENT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gán thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Lịch không ở trạng thái PENDING"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lịch hoặc kỹ thuật viên không tồn tại")
    })
    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<BookingResponse>> assign(
            @PathVariable Long id,
            @Valid @RequestBody BookingAssignRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Gán kỹ thuật viên thành công",
                bookingService.assignTechnician(id, request)));
    }

    @Operation(summary = "Hoàn thành lịch → COMPLETED — TECHNICIAN (được gán), ADMIN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hoàn thành"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Lịch không ở trạng thái CONFIRMED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không phải kỹ thuật viên được gán"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Lịch đã hoàn thành")
    })
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<BookingResponse>> complete(
            @PathVariable Long id,
            @RequestBody(required = false) BookingCompleteRequest request) {
        BookingCompleteRequest req = request != null ? request : new BookingCompleteRequest();
        return ResponseEntity.ok(ApiResponse.success("Hoàn thành lịch bảo trì",
                bookingService.complete(id, req)));
    }

    @Operation(summary = "Hủy lịch → CANCELLED — ADMIN, MANAGEMENT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đã hủy"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Lịch đã hoàn thành, không thể hủy"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Đã hủy lịch bảo trì",
                bookingService.cancel(id)));
    }
}
