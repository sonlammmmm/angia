package vn.dichvuangia.management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.dichvuangia.management.common.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import vn.dichvuangia.management.common.enums.OrderStatus;
import vn.dichvuangia.management.common.enums.PaymentStatus;
import vn.dichvuangia.management.dto.request.OrderCreateRequest;
import vn.dichvuangia.management.dto.request.OrderStatusUpdateRequest;
import vn.dichvuangia.management.dto.response.OrderResponse;
import vn.dichvuangia.management.service.OrderService;

import java.time.LocalDateTime;

@Tag(name = "Orders", description = "Quản lý đơn hàng")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Danh sách đơn hàng — SALE chỉ thấy đơn của mình. Query: status, customerId, from, to")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAll(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getAll(status, customerId, from, to, pageable)));
    }

    @Operation(summary = "Chi tiết đơn hàng kèm danh sách sản phẩm")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getById(id)));
    }

    @Operation(summary = "Tạo đơn hàng — validate tồn kho, sinh order code tự động")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Không đủ tồn kho hoặc dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khách hàng hoặc sản phẩm không tồn tại")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Tạo đơn hàng thành công", orderService.create(request)));
    }

    @Operation(summary = "Cập nhật trạng thái đơn hàng — ADMIN, MANAGEMENT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Chuyển trạng thái không hợp lệ hoặc không đủ tồn kho"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công",
                orderService.updateStatus(id, request)));
    }

    @Operation(summary = "Cập nhật ghi chú đơn hàng — ADMIN, MANAGEMENT, SALE")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @PatchMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<OrderResponse>> updateNotes(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ghi chú thành công",
                orderService.updateNotes(id, body.getOrDefault("notes", ""))));
    }

    @Operation(summary = "Cập nhật nhanh trạng thái thanh toán — ADMIN, MANAGEMENT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<ApiResponse<OrderResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        PaymentStatus status = PaymentStatus.valueOf(body.get("paymentStatus"));
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thanh toán thành công",
                orderService.updatePaymentStatus(id, status)));
    }
}
