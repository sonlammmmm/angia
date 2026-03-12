# Đánh Giá Code Backend — AnGia Management System

> **Trạng thái:** ✅ Backend hoàn thiện, sẵn sàng tích hợp Frontend  
> **Ngày đánh giá:** Tháng 3/2026

---

## 1. Tổng Quan Kiến Trúc

Dự án được xây dựng theo kiến trúc **Layered Architecture** chuẩn:

```
Controller → Service → Repository → Entity (JPA/MySQL)
```

| Thành phần | Mô tả | Số lượng |
|---|---|---|
| Controllers | REST endpoints, xử lý HTTP request/response | 8 |
| Services | Business logic, data-level security | 8 |
| Repositories | JPA data access | 11 |
| Entities | JPA domain models | 10 |
| DTOs | Request/Response objects | 29 |
| Exceptions | Custom + Global handler | 5 |

---

## 2. Những Điểm Đã Đúng ✅

### 2.1 Bảo Mật
- **JWT HMAC-SHA256** với NimbusJwt — tiêu chuẩn Spring Security OAuth2
- **Refresh Token Rotation**: Mỗi token chỉ dùng một lần, xóa cũ trước khi cấp mới → chống replay attack
- **HttpOnly Cookie** cho refresh token → không bị XSS đánh cắp
- **BCrypt** (10 rounds) cho mã hóa password
- **RBAC** phân quyền rõ ràng theo 5 roles: ADMIN, MANAGEMENT, SALE, TECHNICIAN, CUSTOMER

### 2.2 Business Logic
- **State Machine** cho Order và Booking — có validation chuyển trạng thái hợp lệ
- **Snapshot Price**: `OrderItem.unitPrice` lưu giá tại thời điểm mua, không bị ảnh hưởng khi product đổi giá
- **Stock Management**: Validate tồn kho khi tạo đơn, trừ khi COMPLETED, cộng lại khi CANCELLED từ COMPLETED
- **Data-Level Security**: SALE chỉ thấy khách/đơn hàng của mình; TECHNICIAN chỉ thấy lịch được gán
- **Soft Delete**: Sản phẩm, thương hiệu, dịch vụ không bị xóa cứng

### 2.3 Code Quality
- **@Transactional** đúng chỗ — `readOnly = true` cho GET, mặc định cho write
- **Auto-generated codes**: ORD-yyyyMMdd-xxxxx và BK-yyyyMMdd-xxxxx với collision detection (5 lần retry)
- **DataSeeder** idempotent — không seed lại nếu DB đã có dữ liệu
- **Pagination** trên tất cả list endpoints
- **Swagger/OpenAPI** tự động với SpringDoc

---

## 3. Lỗi Đã Phát Hiện & Đã Sửa 🔧

### Lỗi 1: `IllegalArgumentException` Trả Về 500 Thay Vì 400

**File:** `GlobalExceptionHandler.java`  
**Mô tả:** `AuthService.register()` và `CustomerService.create()/update()` throw `IllegalArgumentException` khi trùng username/phone. Exception này không được handle riêng → rơi vào handler generic → trả 500.  
**Sửa:** Thêm `@ExceptionHandler(IllegalArgumentException.class)` → trả HTTP 400.

```java
// ĐÃ SỬA
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage(), "BAD_REQUEST"));
}
```

### Lỗi 2: `SecurityException` Không Được Xử Lý Đúng

**File:** `MaintenanceBookingService.java` — phương thức `complete()`  
**Mô tả:** Khi technician không được gán cố thực hiện complete, code throw `java.lang.SecurityException` (không phải Spring Security). Exception này rơi vào handler generic → trả 500 với debug info.  
**Sửa:** Đổi sang `AccessDeniedException` (Spring Security) → được handler cụ thể bắt, trả 403.

```java
// ĐÃ SỬA
throw new AccessDeniedException("Chỉ kỹ thuật viên được gán mới có thể hoàn thành lịch này");
```

### Lỗi 3: Debug Info Bị Lộ Ra Response (Rủi Ro Bảo Mật)

**File:** `GlobalExceptionHandler.java` — handler fallback  
**Mô tả:** Handler generic 500 trả về class name và message của exception trong response JSON → lộ thông tin kiến trúc nội bộ.  
**Sửa:** Chỉ log ra file (cho dev debug), trả message chung cho client.

```java
// ĐÃ SỬA
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
    log.error("Unhandled exception [{}]: {}", ex.getClass().getName(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Đã xảy ra lỗi hệ thống, vui lòng thử lại sau", "INTERNAL_ERROR"));
}
```

---

## 4. Khuyến Nghị Trước Khi Deploy Production 📋

| # | Hạng mục | Ưu tiên | Ghi chú |
|---|---|---|---|
| 1 | Đổi `secure(false)` → `secure(true)` trong `buildRefreshCookie()` | 🔴 Cao | Chỉ dùng HTTPS khi production |
| 2 | Đổi mật khẩu seed mặc định `admin123` | 🔴 Cao | Hiện hardcode trong DataSeeder |
| 3 | Chuyển DB credentials sang environment variable | 🟡 Trung | Hiện `password=slam7424` trong application.properties |
| 4 | Bật `spring.jpa.show-sql=false` | 🟡 Trung | Đã false, kiểm tra lại production |
| 5 | Cấu hình logging.file.name theo environment | 🟢 Thấp | Hiện hardcode đường dẫn Windows |
| 6 | Thêm rate limiting cho `/auth/login` | 🟡 Trung | Chống brute force |
| 7 | Thêm validation phone trùng khi update customer | 🟢 Thấp | Đã có nhưng cần test kỹ hơn |

---

## 5. Tóm Tắt API Endpoints

> Xem chi tiết tại `API_REFERENCE.md`

| Controller | Base Path | Số Endpoints |
|---|---|---|
| AuthController | `/api/v1/auth` | 4 |
| UserController | `/api/v1/users` | 6 |
| CustomerController | `/api/v1/customers` | 7 |
| ProductController | `/api/v1/products` | 5 |
| BrandController | `/api/v1/brands` | 5 |
| ServiceController | `/api/v1/services` | 5 |
| OrderController | `/api/v1/orders` | 4 |
| MaintenanceBookingController | `/api/v1/maintenance-bookings` | 6 |
| **Tổng** | | **42** |

---

## 6. Kết Luận

Backend AnGia đã được implement đầy đủ, đúng yêu cầu nghiệp vụ. **3 lỗi** được tìm thấy và sửa trong lần review này:

- 2 lỗi logic HTTP status code không đúng (trả 500 thay vì 400/403)
- 1 lỗi bảo mật nhẹ (lộ thông tin exception trong response)

**Backend sẵn sàng 100% để tích hợp với React Frontend.**
