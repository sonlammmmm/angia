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
import vn.dichvuangia.management.dto.request.UserCreateRequest;
import vn.dichvuangia.management.dto.request.UserUpdateRequest;
import vn.dichvuangia.management.dto.response.UserResponse;
import vn.dichvuangia.management.service.UserService;

@Tag(name = "Users", description = "Quản lý tài khoản nhân viên — Chỉ ADMIN")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Danh sách tài khoản nhân viên")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAll(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAll(pageable)));
    }

    @Operation(summary = "Chi tiết tài khoản")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(id)));
    }

    @Operation(summary = "Tạo tài khoản mới")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc username đã tồn tại")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Tạo tài khoản thành công", userService.create(request)));
    }

    @Operation(summary = "Cập nhật thông tin tài khoản")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", userService.update(id, request)));
    }

    @Operation(summary = "Khóa tài khoản")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đã khóa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @PatchMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> lock(@PathVariable Long id) {
        userService.lock(id);
        return ResponseEntity.ok(ApiResponse.success("Đã khóa tài khoản", null));
    }

    @Operation(summary = "Mở khóa tài khoản")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đã mở khóa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @PatchMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlock(@PathVariable Long id) {
        userService.unlock(id);
        return ResponseEntity.ok(ApiResponse.success("Đã mở khóa tài khoản", null));
    }
}
