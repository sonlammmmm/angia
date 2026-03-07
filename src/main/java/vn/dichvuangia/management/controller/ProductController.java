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
import vn.dichvuangia.management.common.enums.ProductType;
import vn.dichvuangia.management.dto.request.ProductCreateRequest;
import vn.dichvuangia.management.dto.request.ProductUpdateRequest;
import vn.dichvuangia.management.dto.response.ProductResponse;
import vn.dichvuangia.management.service.ProductService;

@Tag(name = "Products", description = "Quản lý sản phẩm")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Danh sách sản phẩm (Public). Query: type, brandId")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(
            @RequestParam(required = false) ProductType type,
            @RequestParam(required = false) Long brandId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.getAll(type, brandId, pageable)));
    }

    @Operation(summary = "Chi tiết sản phẩm kèm specs và hình ảnh (Public)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    @Operation(summary = "Tạo sản phẩm mới — ADMIN, MANAGEMENT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc mã sản phẩm đã tồn tại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Thương hiệu không tồn tại")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Tạo sản phẩm thành công", productService.create(request)));
    }

    @Operation(summary = "Cập nhật sản phẩm — ADMIN, MANAGEMENT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", productService.update(id, request)));
    }

    @Operation(summary = "Xóa sản phẩm (soft delete) — ADMIN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đã xóa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa sản phẩm", null));
    }
}
