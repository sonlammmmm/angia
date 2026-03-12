# API Reference — AnGia Management System

> **Base URL:** `http://localhost:8080/api/v1`  
> **Authentication:** Bearer Token (JWT) trong header `Authorization`  
> **Refresh Token:** HttpOnly Cookie tên `refreshToken`  
> **Content-Type:** `application/json`

---

## Cấu Trúc Response Chung

Mọi response đều bọc trong `ApiResponse<T>`:

```json
{
  "status": "success" | "error",
  "message": "Mô tả kết quả",
  "data": { ... } | null,
  "errorCode": null | "ERROR_CODE"
}
```

**Các errorCode phổ biến:**

| errorCode | HTTP | Ý nghĩa |
|---|---|---|
| `RESOURCE_NOT_FOUND` | 404 | Không tìm thấy resource |
| `VALIDATION_ERROR` | 400 | Dữ liệu không hợp lệ (data là object field-message) |
| `BAD_REQUEST` | 400 | Dữ liệu không hợp lệ (trùng phone, username,...) |
| `INSUFFICIENT_STOCK` | 400 | Không đủ tồn kho |
| `INVALID_STATUS_TRANSITION` | 400 | Chuyển trạng thái không hợp lệ |
| `INVALID_CREDENTIALS` | 401 | Sai username/password |
| `UNAUTHORIZED` | 401 | Chưa đăng nhập |
| `ACCOUNT_DISABLED` | 403 | Tài khoản bị khóa |
| `ACCESS_DENIED` | 403 | Không có quyền |
| `BOOKING_ALREADY_COMPLETED` | 409 | Lịch đã hoàn thành, không thể thay đổi |
| `INTERNAL_ERROR` | 500 | Lỗi hệ thống |

---

## 1. Authentication (`/auth`)

> **Tất cả public — không cần token**

### POST `/auth/register`

Đăng ký tài khoản khách hàng. Tự động đăng nhập và cấp token.

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123",
  "fullName": "Nguyễn Văn A",
  "phone": "0901234567",
  "address": "123 Đường ABC, TP.HCM"
}
```

| Field | Kiểu | Bắt buộc | Ràng buộc |
|---|---|---|---|
| `username` | String | ✅ | 3-100 ký tự |
| `password` | String | ✅ | Tối thiểu 6 ký tự |
| `fullName` | String | ✅ | Không trống |
| `phone` | String | ✅ | Regex: `^(0|\+84)[0-9]{9,10}$` |
| `address` | String | ❌ | — |

**Response 201:**
```json
{
  "status": "success",
  "message": "Đăng ký thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```
> ⚠️ `refreshToken` **KHÔNG** có trong body JSON — được set trong HttpOnly Cookie `refreshToken`.

---

### POST `/auth/login`

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response 200:** Giống cấu trúc register.

**Response 401:** `INVALID_CREDENTIALS`  
**Response 403:** `ACCOUNT_DISABLED`

---

### POST `/auth/refresh`

Làm mới Access Token dùng Refresh Token từ Cookie.

> Không cần request body. Cookie `refreshToken` được tự động gửi.

**Response 200:**
```json
{
  "status": "success",
  "message": "Cấp token mới thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "newRefreshToken": "uuid-string-here"
  }
}
```
> ⚠️ `newRefreshToken` chỉ để tham khảo. Cookie `refreshToken` được cập nhật tự động.

**Response 401:** `UNAUTHORIZED` — cookie không có hoặc token hết hạn.

---

### POST `/auth/logout`

Xóa refresh token. Cookie `refreshToken` được clear.

**Response 200:**
```json
{
  "status": "success",
  "message": "Đăng xuất thành công",
  "data": null
}
```

---

## 2. Users (`/users`)

> 🔒 **ADMIN only**

### GET `/users`

Danh sách tài khoản nhân viên (phân trang).

**Query Params:** `page=0&size=10&sort=createdAt,desc`

**Response 200:**
```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "username": "admin",
        "roleName": "ADMIN",
        "isActive": true,
        "createdAt": "2025-01-01T00:00:00"
      }
    ],
    "totalElements": 4,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

---

### GET `/users/{id}`

**Response 200:** `UserResponse` (single object)

---

### POST `/users`

Tạo tài khoản nhân viên.

**Request Body:**
```json
{
  "username": "sale02",
  "password": "password123",
  "roleId": 3
}
```

**Roles seeded:**
| roleId | Tên |
|---|---|
| 1 | ADMIN |
| 2 | MANAGEMENT |
| 3 | SALE |
| 4 | TECHNICIAN |
| 5 | CUSTOMER |

**Response 201:** `UserResponse`

---

### PUT `/users/{id}`

Cập nhật tài khoản.

**Request Body:** (tất cả optional)
```json
{
  "password": "newpassword",
  "roleId": 4,
  "isActive": true
}
```

---

### PATCH `/users/{id}/lock`

Khóa tài khoản (`isActive = false`). **Response 200.**

### PATCH `/users/{id}/unlock`

Mở khóa tài khoản (`isActive = true`). **Response 200.**

---

## 3. Customers (`/customers`)

### GET `/customers/me`

> 🔒 **Authenticated** (thường dùng cho CUSTOMER role)

Xem hồ sơ khách hàng của chính mình.

**Response 200:**
```json
{
  "data": {
    "id": 5,
    "fullName": "Nguyễn Văn A",
    "phone": "0901234567",
    "address": "123 Đường ABC",
    "createdAt": "2025-01-15T10:30:00",
    "createdById": 5,
    "createdByUsername": "john_doe"
  }
}
```

---

### PUT `/customers/me`

> 🔒 **Authenticated**

Cập nhật hồ sơ của chính mình.

**Request Body:** (tất cả optional)
```json
{
  "fullName": "Tên Mới",
  "phone": "0909999999",
  "address": "Địa chỉ mới"
}
```

---

### GET `/customers`

> 🔒 **ADMIN / MANAGEMENT / SALE**  
> SALE chỉ thấy khách hàng do mình tạo.

**Query Params:** `page=0&size=10&sort=createdAt,desc`

---

### GET `/customers/{id}`

> 🔒 **ADMIN / MANAGEMENT / SALE**

---

### POST `/customers`

> 🔒 **ADMIN / MANAGEMENT / SALE**

Tạo khách hàng mới (nhân viên tạo hộ).

**Request Body:**
```json
{
  "fullName": "Trần Thị B",
  "phone": "0912345678",
  "address": "456 Đường XYZ"
}
```

---

### PUT `/customers/{id}`

> 🔒 **ADMIN / MANAGEMENT / SALE**

---

### GET `/customers/{id}/bookings`

> 🔒 **ADMIN / MANAGEMENT / SALE**

Lịch sử đặt lịch bảo trì của khách hàng.

**Query Params:** `page=0&size=10`

**Response 200:** Page của `BookingResponse`

---

## 4. Products (`/products`)

### GET `/products`

> 🌐 **Public**

**Query Params:**

| Param | Kiểu | Mô tả |
|---|---|---|
| `type` | `MACHINE` \| `FILTER` | Lọc theo loại |
| `brandId` | Long | Lọc theo thương hiệu |
| `page` | int | Trang (default 0) |
| `size` | int | Kích thước trang (default 10) |

**Response 200:**
```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "productCode": "AQ-001",
        "productType": "MACHINE",
        "brandId": 1,
        "brandName": "Aqua",
        "name": "Máy lọc nước Aqua A5",
        "model": "A5-2024",
        "description": "Mô tả sản phẩm...",
        "price": 5500000.00,
        "stockQuantity": 15,
        "warrantyMonths": 24,
        "lifespanMonths": 60,
        "specsJson": {
          "capacity": "10L/hour",
          "stages": 9
        },
        "images": [
          {
            "id": 1,
            "imageUrl": "https://...",
            "isMain": true
          }
        ],
        "createdAt": "2025-01-01T00:00:00"
      }
    ],
    "totalElements": 20,
    "totalPages": 2
  }
}
```

---

### GET `/products/{id}`

> 🌐 **Public**

---

### POST `/products`

> 🔒 **ADMIN / MANAGEMENT**

**Request Body:**
```json
{
  "productCode": "KG-002",
  "productType": "FILTER",
  "brandId": 2,
  "name": "Lõi lọc Kangaroo",
  "model": "KG-F100",
  "description": "Mô tả...",
  "price": 250000.00,
  "stockQuantity": 100,
  "warrantyMonths": 6,
  "lifespanMonths": 6,
  "specsJson": {
    "material": "CTO Carbon",
    "micron": 5
  }
}
```

**Response 201:** `ProductResponse`

---

### PUT `/products/{id}`

> 🔒 **ADMIN / MANAGEMENT**

**Request Body:** Tất cả optional, giống POST nhưng không có `productCode` và `productType`.

---

### DELETE `/products/{id}`

> 🔒 **ADMIN only** — Soft delete (`isDeleted = true`)

**Response 200.**

---

## 5. Brands (`/brands`)

### GET `/brands` — 🌐 Public

### GET `/brands/{id}` — 🌐 Public

**Response:**
```json
{
  "data": {
    "id": 1,
    "name": "Aqua",
    "description": "Thương hiệu máy lọc nước Aqua Nhật Bản",
    "logoUrl": null,
    "createdAt": "2025-01-01T00:00:00"
  }
}
```

### POST `/brands` — 🔒 ADMIN / MANAGEMENT

```json
{
  "name": "Tên thương hiệu",
  "description": "Mô tả",
  "logoUrl": "https://example.com/logo.png"
}
```

### PUT `/brands/{id}` — 🔒 ADMIN / MANAGEMENT

### DELETE `/brands/{id}` — 🔒 ADMIN (Soft delete)

---

## 6. Services (`/services`)

### GET `/services` — 🌐 Public

### GET `/services/{id}` — 🌐 Public

**Response:**
```json
{
  "data": {
    "id": 1,
    "serviceCode": null,
    "name": "Vệ sinh bộ lọc",
    "description": "Vệ sinh và thay lõi lọc định kỳ",
    "basePrice": 150000.00,
    "durationMinutes": 60,
    "createdAt": "2025-01-01T00:00:00"
  }
}
```

### POST `/services` — 🔒 ADMIN / MANAGEMENT

```json
{
  "serviceCode": "SV-001",
  "name": "Thay lõi lọc",
  "description": "Mô tả dịch vụ",
  "basePrice": 200000.00,
  "durationMinutes": 45
}
```

### PUT `/services/{id}` — 🔒 ADMIN / MANAGEMENT

### DELETE `/services/{id}` — 🔒 ADMIN (Soft delete)

---

## 7. Orders (`/orders`)

### GET `/orders`

> 🔒 **Authenticated**  
> SALE: chỉ thấy đơn của mình. ADMIN/MANAGEMENT: thấy tất cả.

**Query Params:**

| Param | Kiểu | Mô tả |
|---|---|---|
| `status` | `PENDING\|PROCESSING\|COMPLETED\|CANCELLED` | Lọc theo trạng thái |
| `customerId` | Long | Lọc theo khách hàng |
| `page`, `size`, `sort` | Pageable | Phân trang |

**Response 200:**
```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "orderCode": "ORD-20250115-12345",
        "status": "PENDING",
        "totalAmount": 11000000.00,
        "shippingAddress": "123 Đường ABC",
        "createdAt": "2025-01-15T10:00:00",
        "updatedAt": "2025-01-15T10:00:00",
        "customerId": 5,
        "customerName": "Nguyễn Văn A",
        "customerPhone": "0901234567",
        "saleId": 3,
        "saleUsername": "sale01",
        "items": [
          {
            "id": 1,
            "productId": 1,
            "productName": "Máy lọc nước Aqua A5",
            "productCode": "AQ-001",
            "quantity": 2,
            "unitPrice": 5500000.00,
            "subtotal": 11000000.00
          }
        ]
      }
    ]
  }
}
```

---

### GET `/orders/{id}` — 🔒 Authenticated

---

### POST `/orders`

> 🔒 **Authenticated**

Tạo đơn hàng. Validate tồn kho trước khi tạo.

**Request Body:**
```json
{
  "customerId": 5,
  "shippingAddress": "123 Đường ABC, TP.HCM",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

**Response 201:** `OrderResponse`  
**Response 400:** `INSUFFICIENT_STOCK` — không đủ tồn kho  
**Response 404:** `RESOURCE_NOT_FOUND` — khách hàng hoặc sản phẩm không tồn tại

---

### PATCH `/orders/{id}/status`

> 🔒 **ADMIN / MANAGEMENT**

**State Machine:**
```
PENDING → PROCESSING → COMPLETED
    ↘         ↘          ↘
     CANCELLED  CANCELLED  CANCELLED (hoàn stock)
```

**Request Body:**
```json
{
  "status": "PROCESSING"
}
```

**Giá trị hợp lệ:** `PENDING`, `PROCESSING`, `COMPLETED`, `CANCELLED`

**Response 200:** `OrderResponse`  
**Response 400:** `INVALID_STATUS_TRANSITION`

---

## 8. Maintenance Bookings (`/maintenance-bookings`)

### GET `/maintenance-bookings`

> 🔒 **Authenticated**  
> TECHNICIAN: chỉ thấy lịch được gán. ADMIN/MANAGEMENT: thấy tất cả.

**Query Params:**

| Param | Kiểu | Mô tả |
|---|---|---|
| `status` | `PENDING\|CONFIRMED\|COMPLETED\|CANCELLED` | Lọc trạng thái |
| `customerId` | Long | Lọc theo khách hàng |
| `from` | ISO DateTime | Từ ngày (VD: `2025-01-01T00:00:00`) |
| `to` | ISO DateTime | Đến ngày |
| `page`, `size`, `sort` | Pageable | Phân trang |

**Response 200:**
```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "bookingCode": "BK-20250115-54321",
        "status": "PENDING",
        "bookingDate": "2025-01-20T09:00:00",
        "notes": "Ghi chú của khách",
        "createdAt": "2025-01-15T10:00:00",
        "customerId": 5,
        "customerName": "Nguyễn Văn A",
        "customerPhone": "0901234567",
        "customerAddress": "123 Đường ABC",
        "serviceId": 1,
        "serviceName": "Vệ sinh bộ lọc",
        "serviceBasePrice": 150000.00,
        "technicianId": null,
        "technicianUsername": null
      }
    ]
  }
}
```

---

### GET `/maintenance-bookings/{id}` — 🔒 Authenticated

---

### POST `/maintenance-bookings`

> 🔒 **Authenticated**

Đặt lịch bảo trì. Trạng thái ban đầu = PENDING, kỹ thuật viên = null.

**Request Body:**
```json
{
  "customerId": 5,
  "serviceId": 1,
  "bookingDate": "2025-01-20T09:00:00",
  "notes": "Máy lọc bị rò rỉ"
}
```

**Response 201:** `BookingResponse`

---

### PATCH `/maintenance-bookings/{id}/assign`

> 🔒 **ADMIN / MANAGEMENT**

Gán kỹ thuật viên. Chuyển trạng thái PENDING → CONFIRMED.

**Request Body:**
```json
{
  "technicianId": 4
}
```

**Response 200:** `BookingResponse`  
**Response 400:** Booking không ở trạng thái PENDING

---

### PATCH `/maintenance-bookings/{id}/complete`

> 🔒 **ADMIN / TECHNICIAN**  
> Chỉ kỹ thuật viên được gán mới được hoàn thành.

**Request Body:** (optional)
```json
{
  "notes": "Đã thay lõi lọc và vệ sinh máy"
}
```

**Chuyển trạng thái:** CONFIRMED → COMPLETED

**Response 200:** `BookingResponse`  
**Response 403:** Không phải kỹ thuật viên được gán  
**Response 409:** Lịch đã COMPLETED

---

### PATCH `/maintenance-bookings/{id}/cancel`

> 🔒 **ADMIN / MANAGEMENT**

Hủy lịch. Chỉ hủy được PENDING hoặc CONFIRMED.

**Response 200:** `BookingResponse`  
**Response 409:** Lịch đã COMPLETED — không thể hủy

---

## 9. Tài Khoản Mặc Định (Seed Data)

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `manager01` | `admin123` | MANAGEMENT |
| `sale01` | `admin123` | SALE |
| `tech01` | `admin123` | TECHNICIAN |

**Thương hiệu:** Aqua (id:1), Kangaroo (id:2), Karofi (id:3), Sunhouse (id:4)

**Dịch vụ:**
| ID | Tên | Giá |
|---|---|---|
| 1 | Vệ sinh bộ lọc | 150,000đ |
| 2 | Sửa chữa máy lọc nước | 300,000đ |
| 3 | Lắp đặt máy mới | 200,000đ |
| 4 | Thay màng RO | 400,000đ |
| 5 | Kiểm tra định kỳ | 80,000đ |

---

## 10. Swagger UI

Sau khi khởi động server, truy cập:
```
http://localhost:8080/swagger-ui/index.html
```

API Docs JSON:
```
http://localhost:8080/v3/api-docs
```
