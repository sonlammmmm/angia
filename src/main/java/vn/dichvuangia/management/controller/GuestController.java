package vn.dichvuangia.management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.dichvuangia.management.common.ApiResponse;
import vn.dichvuangia.management.dto.request.GuestBookingCreateRequest;
import vn.dichvuangia.management.dto.request.GuestOrderCreateRequest;
import vn.dichvuangia.management.dto.response.BookingResponse;
import vn.dichvuangia.management.dto.response.OrderResponse;
import vn.dichvuangia.management.service.MaintenanceBookingService;
import vn.dichvuangia.management.service.OrderService;

/**
 * Endpoints dành cho khách vãng lai — không yêu cầu đăng nhập.
 * Khách chỉ cần cung cấp họ tên, số điện thoại, địa chỉ.
 */
@Tag(name = "Guest", description = "Đặt hàng / đặt lịch cho khách vãng lai (không cần đăng nhập)")
@RestController
@RequestMapping("/guest")
@RequiredArgsConstructor
public class GuestController {

    private final OrderService orderService;
    private final MaintenanceBookingService bookingService;

    @Operation(summary = "Khách vãng lai đặt hàng — chỉ cần nhập thông tin cá nhân")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Đặt hàng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc không đủ tồn kho"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Sản phẩm không tồn tại")
    })
    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createGuestOrder(
            @Valid @RequestBody GuestOrderCreateRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Đặt hàng thành công", orderService.createGuest(request)));
    }

    @Operation(summary = "Khách vãng lai đặt lịch bảo trì — chỉ cần nhập thông tin cá nhân")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Đặt lịch thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dịch vụ không tồn tại")
    })
    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponse>> createGuestBooking(
            @Valid @RequestBody GuestBookingCreateRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Đặt lịch thành công", bookingService.createGuest(request)));
    }
}
