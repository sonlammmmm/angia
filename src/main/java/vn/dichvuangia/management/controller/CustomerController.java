package vn.dichvuangia.management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.dichvuangia.management.common.ApiResponse;
import vn.dichvuangia.management.dto.request.CustomerCreateRequest;
import vn.dichvuangia.management.dto.request.CustomerUpdateRequest;
import vn.dichvuangia.management.dto.response.BookingResponse;
import vn.dichvuangia.management.dto.response.CustomerResponse;
import vn.dichvuangia.management.service.CustomerService;

@Tag(name = "Customers", description = "Quản lý khách hàng")
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // ── Endpoints dành cho CUSTOMER (tự xem/sửa hồ sơ) ────────────────────────

    @Operation(summary = "Khách hàng xem hồ sơ của chính mình")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Chưa có hồ sơ khách hàng")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(customerService.getMyProfile()));
    }

    @Operation(summary = "Khách hàng cập nhật hồ sơ của chính mình")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Số điện thoại đã tồn tại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Chưa có hồ sơ khách hàng")
    })
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateMyProfile(
            @Valid @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", customerService.updateMyProfile(request)));
    }

    // ── Endpoints dành cho nhân viên (ADMIN, MANAGEMENT, SALE) ─────────────────

    @Operation(summary = "Danh sách khách hàng — SALE chỉ thấy khách do mình tạo. Query: q (tìm theo tên/SĐT)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> getAll(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAll(q, pageable)));
    }

    @Operation(summary = "Chi tiết khách hàng")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getById(id)));
    }

    @Operation(summary = "Tra cứu khách hàng theo SĐT — trả về null nếu không tìm thấy")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công (data có thể null)")
    })
    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<CustomerResponse>> lookupByPhone(
            @RequestParam String phone) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findByPhone(phone).orElse(null)));
    }

    @Operation(summary = "Tạo hồ sơ khách hàng mới")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Số điện thoại đã tồn tại")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody CustomerCreateRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Tạo khách hàng thành công", customerService.create(request)));
    }

    @Operation(summary = "Cập nhật thông tin khách hàng")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", customerService.update(id, request)));
    }

    @Operation(summary = "Lịch sử đặt lịch bảo trì của khách hàng")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khách hàng không tồn tại")
    })
    @GetMapping("/{id}/bookings")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getBookings(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getBookings(id, pageable)));
    }
}
