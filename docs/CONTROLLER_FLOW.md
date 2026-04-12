# Tài liệu luồng xử lý Backend (Controllers/Services/Repositories/Entities)

Ngày cập nhật: 2026-04-13

## 1) Tổng quan kiến trúc

- **Controller**: nhận request HTTP/WS, validate input (Bean Validation), gọi Service, trả về `ApiResponse`.
- **Service**: xử lý nghiệp vụ, kiểm tra quyền, điều kiện dữ liệu, transaction.
- **Repository**: thao tác DB qua Spring Data JPA.
- **Entity**: mô hình dữ liệu (JPA).
- **DTO**: request/response payload cho API.
- **Security**: JWT + Spring Security Resource Server, role-based access.

### 1.1. Chuẩn response
- File: `vn/dichvuangia/management/common/ApiResponse.java`
- Mục đích: bọc response theo schema thống nhất.
- `success(data)`/`success(message, data)` và `error(message, errorCode)`.

### 1.2. Bảo mật & JWT
- File: `security/SecurityConfig.java`
  - Cấu hình JWT HMAC-SHA256, stateless.
  - Authorization rules theo endpoint/role.
- File: `security/JwtService.java`
  - Tạo Access Token (claim: `userId`, `scope`).
- File: `security/UserDetailsServiceImpl.java`
  - Nạp user theo username để login.

**Luồng xác thực tổng quát**
1. Login/Register/Google → `AuthService` tạo AccessToken + RefreshToken.
2. AccessToken gửi ở `Authorization: Bearer` cho các request protected.
3. RefreshToken lưu ở cookie HttpOnly (controller set cookie).

## 2) Auth (Đăng ký/Đăng nhập/Refresh/Logout)

### 2.1. Controller: `AuthController`
File: `controller/AuthController.java`

- **POST `/auth/register`**
  - Input: `RegisterRequest` (`dto/request/RegisterRequest.java`)
  - Biến dùng:
    - `request`: dữ liệu đăng ký → `authService.register(request)`
    - `authResponse`: kết quả login tự động → lấy `refreshToken` để set cookie
  - Luồng:
    1. Validate input.
    2. `AuthService.register` tạo User + Customer + token.
    3. Build cookie `refreshToken` (HttpOnly).
    4. Trả `ApiResponse<AuthResponse>` (refreshToken bị `@JsonIgnore`).

- **POST `/auth/login`**
  - Input: `LoginRequest` (`dto/request/LoginRequest.java`)
  - Biến dùng:
    - `request` → `authService.login(request)`
    - `authResponse` → set cookie

- **POST `/auth/google`**
  - Input: `GoogleLoginRequest` (`dto/request/GoogleLoginRequest.java`)
  - Biến dùng:
    - `request` → `authService.googleLogin(request)`

- **POST `/auth/refresh`**
  - Input: cookie `refreshToken`
  - Biến dùng:
    - `refreshToken` (cookie) → `authService.refresh(refreshToken)`
    - `result` → set cookie mới

- **POST `/auth/logout`**
  - Input: cookie `refreshToken`
  - Biến dùng:
    - `refreshToken` → `authService.logout(refreshToken)`
    - `clearCookie` → set maxAge=0

- **PUT `/auth/change-password`**
  - Input: `ChangePasswordRequest` (`dto/request/ChangePasswordRequest.java`)
  - Biến dùng:
    - `request` → `authService.changePassword(request)`

### 2.2. Service: `AuthService`
File: `service/AuthService.java`

- `register(RegisterRequest)`:
  - Kiểm tra trùng username/phone (`UserRepository`, `CustomerRepository`).
  - Lấy role CUSTOMER (`RoleRepository`).
  - Tạo `User`, `Customer`.
  - Sinh AccessToken (`JwtService`) + RefreshToken (`RefreshTokenRepository`).

- `login(LoginRequest)`:
  - `AuthenticationManager` xác thực.
  - Tìm `User` → sinh token.

- `googleLogin(GoogleLoginRequest)`:
  - Verify Google ID token (`GoogleIdTokenVerifier`).
  - Tạo user + customer nếu chưa tồn tại.

- `refresh(rawToken)`:
  - Kiểm tra token & hạn (`RefreshTokenRepository`).
  - Xóa token cũ (rotation) → tạo token mới.

- `logout(rawToken)`:
  - Xóa refresh token.

- `changePassword(ChangePasswordRequest)`:
  - Đọc `userId` từ JWT (`SecurityContextHolder`).
  - Validate mật khẩu cũ/mới.

### 2.3. Repository/Entity
- `UserRepository`, `RoleRepository`, `CustomerRepository`, `RefreshTokenRepository`.
- Entities: `User`, `Role`, `Customer`, `RefreshToken`.

### 2.4. DTO liên quan
- `RegisterRequest`, `LoginRequest`, `GoogleLoginRequest`, `ChangePasswordRequest`.
- `AuthResponse`, `TokenRefreshResponse`.

### 2.5. Input/Output/Validation/Side Effects (module khó)

**POST `/auth/register`**
- Input: `RegisterRequest {username, password, fullName, phone, address}`
- Output: `ApiResponse<AuthResponse>` (body chỉ có access token; refresh token trả trong cookie)
- Validation:
  - `username` min 3, `password` min 6, `phone` regex VN.
  - Service check trùng `username` và `phone`.
- Side effects:
  - Tạo `User` (role CUSTOMER) + `Customer`.
  - Tạo `RefreshToken` (DB) + set cookie HttpOnly.

**POST `/auth/login`**
- Input: `LoginRequest {username, password}`
- Output: `ApiResponse<AuthResponse>`
- Validation:
  - Bean Validation not blank.
  - Spring Security xác thực username/password.
- Side effects:
  - Tạo refresh token + set cookie.

**POST `/auth/google`**
- Input: `GoogleLoginRequest {credential}`
- Output: `ApiResponse<AuthResponse>`
- Validation:
  - `credential` not blank.
  - Verify Google ID token.
- Side effects:
  - Tạo User/Customer nếu chưa tồn tại.

**POST `/auth/refresh`**
- Input: cookie `refreshToken`
- Output: `ApiResponse<TokenRefreshResponse>`
- Validation:
  - Token tồn tại và chưa hết hạn.
- Side effects:
  - Rotation: xóa token cũ, tạo refresh token mới, set cookie mới.

**PUT `/auth/change-password`**
- Input: `ChangePasswordRequest {currentPassword, newPassword, confirmPassword}`
- Output: `ApiResponse<Void>`
- Validation:
  - newPassword == confirmPassword.
  - currentPassword đúng.
- Side effects:
  - Update `User.passwordHash`.

### 2.6. Sequence (Auth)
1. Client → `/auth/login`
2. `AuthController` → `AuthService.login`
3. `AuthenticationManager` xác thực
4. `AuthService` tạo access + refresh token
5. Controller set cookie refresh và trả body access token

### 2.7. Deep dive: `AuthService` (method-by-method)

**`register(RegisterRequest)`**
- Input:
  - `username`, `password`, `fullName`, `phone`, `address?`
- Output:
  - `AuthResponse {accessToken, tokenType, expiresIn, refreshToken}`
- Validation:
  - `username` trùng → `IllegalArgumentException`.
  - `phone` trùng → `IllegalArgumentException`.
  - Role `CUSTOMER` phải tồn tại.
- Side effects:
  - Tạo `User` (role CUSTOMER, isActive=true).
  - Tạo `Customer` với `createdBy=user`.
  - Sinh JWT + lưu `RefreshToken`.
- Edge cases:
  - Role chưa khởi tạo → `ResourceNotFoundException`.

**`login(LoginRequest)`**
- Input: `username`, `password`.
- Output: `AuthResponse` (access + refresh).
- Validation:
  - Sai mật khẩu → Spring Security ném lỗi 401/403.
- Side effects:
  - Lưu `RefreshToken` mới.

**`googleLogin(GoogleLoginRequest)`**
- Input: `credential` (Google ID token).
- Output: `AuthResponse`.
- Validation:
  - Verify ID token (audience = `googleClientId`).
- Side effects:
  - Nếu user chưa có → tạo `User` + `Customer`.

**`refresh(String rawToken)`**
- Input: refresh token string.
- Output: `TokenRefreshResponse` (accessToken + newRefreshToken).
- Validation:
  - Token không tồn tại → `ResourceNotFoundException`.
  - Token hết hạn → `IllegalStateException` + delete token.
- Side effects:
  - Rotation: xóa token cũ, tạo token mới.

**`logout(String rawToken)`**
- Input: refresh token string.
- Output: void.
- Side effects:
  - Xóa refresh token khỏi DB nếu tồn tại.

**`changePassword(ChangePasswordRequest)`**
- Input: currentPassword, newPassword, confirmPassword.
- Output: void.
- Validation:
  - confirm mismatch → `IllegalArgumentException`.
  - currentPassword sai → `IllegalArgumentException`.
  - newPassword trùng password cũ → `IllegalArgumentException`.
- Side effects:
  - Update `User.passwordHash`.

## 3) Users (Quản lý tài khoản nhân viên)

### 3.1. Controller: `UserController`
File: `controller/UserController.java`

- **GET `/users`**
  - `pageable` → `userService.getAll(pageable)`
- **GET `/users/{id}`**
  - `id` → `userService.getById(id)`
- **POST `/users`**
  - `UserCreateRequest request` → `userService.create(request)`
- **PUT `/users/{id}`**
  - `id`, `UserUpdateRequest request` → `userService.update(id, request)`
- **PATCH `/users/{id}/lock`**
  - `id` → `userService.lock(id)`
- **PATCH `/users/{id}/unlock`**
  - `id` → `userService.unlock(id)`

### 3.2. Service: `UserService`
File: `service/UserService.java`

- `getAll(Pageable)`: lấy nhân viên (trừ CUSTOMER) → `UserRepository.findAllStaff`.
- `getById(Long)`: đọc theo id.
- `create(UserCreateRequest)`: kiểm tra username trùng, validate role (chặn gán ADMIN nếu không phải ADMIN), lưu user.
- `update(Long, UserUpdateRequest)`: cập nhật fullName, password, role, isActive.
- `lock/unlock`: set `isActive`.

### 3.3. DTO/Entity/Repository
- DTO: `UserCreateRequest`, `UserUpdateRequest`, `UserResponse`.
- Entity: `User`, `Role`.
- Repository: `UserRepository`, `RoleRepository`.

### 3.4. Input/Output/Validation/Side Effects

**POST `/users`**
- Input: `UserCreateRequest {username, password, roleId}`
- Output: `ApiResponse<UserResponse>`
- Validation:
  - `username`, `password`, `roleId` not null.
  - `username` không trùng.
  - Chỉ ADMIN được gán role ADMIN.
- Side effects:
  - Tạo `User` (isActive=true).

**PUT `/users/{id}`**
- Input: `UserUpdateRequest {fullName?, password?, roleId?, isActive?}`
- Output: `ApiResponse<UserResponse>`
- Validation:
  - Nếu đổi role ADMIN → chỉ ADMIN được phép.
- Side effects:
  - Update các field được gửi.

### 3.5. Sequence (Create user)
1. Client → `/users`
2. Service validate username + role
3. Encode password → save User
4. Trả `UserResponse`

### 3.6. Deep dive: `UserService`

**`getAll(Pageable)`**
- Input: `pageable`.
- Output: `Page<UserResponse>`.
- Validation:
  - Chỉ lấy staff (`findAllStaff`).
- Side effects: none.

**`getById(Long)`**
- Input: id.
- Output: `UserResponse`.
- Validation:
  - User tồn tại.

**`create(UserCreateRequest)`**
- Input: `username`, `password`, `roleId`.
- Output: `UserResponse`.
- Validation:
  - Username unique.
  - Role tồn tại.
  - Chỉ ADMIN được gán ADMIN.
- Side effects:
  - Tạo `User`.

**`update(Long, UserUpdateRequest)`**
- Input: id + DTO update.
- Output: `UserResponse`.
- Side effects:
  - Update fields được gửi.

**`lock(Long)` / `unlock(Long)`**
- Input: id.
- Output: void.
- Side effects:
  - Set `isActive=false/true`.

## 4) Brands (Thương hiệu)

### 4.1. Controller: `BrandController`
- GET `/brands`: `pageable` → `brandService.getAll(pageable)`
- GET `/brands/{id}`: `id` → `brandService.getById(id)`
- POST `/brands`: `BrandCreateRequest` → `brandService.create(request)`
- PUT `/brands/{id}`: `id`, `BrandUpdateRequest` → `brandService.update(id, request)`
- DELETE `/brands/{id}`: `id` → `brandService.softDelete(id)`

### 4.2. Service: `BrandService`
- `getAll`: lọc `isDeleted=false`.
- `getById`: gọi `findActiveBrand`.
- `create`: tạo entity `Brand` và set `isDeleted=false`.
- `update`: cập nhật field nếu request có giá trị.
- `softDelete`: set `isDeleted=true`.

### 4.3. DTO/Entity/Repository
- DTO: `BrandCreateRequest`, `BrandUpdateRequest`, `BrandResponse`.
- Entity: `Brand`.
- Repository: `BrandRepository`.

### 4.4. Input/Output/Validation/Side Effects

**POST `/brands`**
- Input: `BrandCreateRequest {name, description?, logoUrl?}`
- Output: `ApiResponse<BrandResponse>`
- Validation:
  - `name` not blank.
- Side effects:
  - Tạo `Brand` (isDeleted=false).

**PUT `/brands/{id}`**
- Input: `BrandUpdateRequest` (field nullable)
- Output: `ApiResponse<BrandResponse>`
- Side effects:
  - Update name/description/logoUrl nếu có.

### 4.5. Sequence (Brand update)
1. Client → `/brands/{id}`
2. Service find brand (not deleted)
3. Update fields, save
4. Trả `BrandResponse`

### 4.6. Deep dive: `BrandService`

**`getAll(Pageable)`**
- Input: pageable.
- Output: `Page<BrandResponse>`.
- Side effects: none.

**`getById(Long)`**
- Input: id.
- Output: `BrandResponse`.
- Validation:
  - Chỉ brand active.

**`create(BrandCreateRequest)`**
- Input: name, description?, logoUrl?.
- Output: `BrandResponse`.
- Side effects:
  - Tạo brand với `isDeleted=false`.

**`update(Long, BrandUpdateRequest)`**
- Input: id + DTO update.
- Output: `BrandResponse`.
- Side effects:
  - Update các field không null.

**`softDelete(Long)`**
- Input: id.
- Output: void.
- Side effects:
  - Set `isDeleted=true`.

## 5) Products (Sản phẩm)

### 5.1. Controller: `ProductController`
- GET `/products`: filter `type`, `brandId`, `q`, `pageable` → `productService.getAll(...)`
- GET `/products/{id}`: `id` → `productService.getById(id)`
- POST `/products`: `ProductCreateRequest` → `productService.create(request)`
- PUT `/products/{id}`: `id`, `ProductUpdateRequest` → `productService.update(id, request)`
- DELETE `/products/{id}`: `id` → `productService.softDelete(id)`

### 5.2. Service: `ProductService`
- `getAll`: filter theo type/brand/q.
- `getById`: lấy sản phẩm đang active.
- `create`: validate mã sản phẩm, set thông tin, serialize specs, lưu ảnh.
- `update`: cập nhật field, replace ảnh nếu có `imageUrls`.
- `softDelete`: set `isDeleted=true`.

### 5.3. DTO/Entity/Repository
- DTO: `ProductCreateRequest`, `ProductUpdateRequest`, `ProductResponse`, `ProductImageResponse`.
- Entity: `Product`, `ProductImage`, `Brand`.
- Repository: `ProductRepository`, `ProductImageRepository`, `BrandRepository`.

### 5.4. Input/Output/Validation/Side Effects

**POST `/products`**
- Input: `ProductCreateRequest` (productCode, productType, brandId, name, price, stockQuantity, specsJson?, imageUrls?)
- Output: `ApiResponse<ProductResponse>`
- Validation:
  - `productCode` unique.
  - `brandId` tồn tại và active.
  - `price > 0`, `stockQuantity >= 0`.
- Side effects:
  - Serialize specsJson → lưu DB.
  - Tạo `ProductImage` nếu có `imageUrls` (ảnh đầu là main).

**PUT `/products/{id}`**
- Input: `ProductUpdateRequest` (field nullable)
- Output: `ApiResponse<ProductResponse>`
- Side effects:
  - Update product fields.
  - Nếu `imageUrls` != null → xóa ảnh cũ và tạo ảnh mới.

### 5.5. Sequence (Product create)
1. Client → `/products`
2. Validate code/brand/price
3. Save Product
4. Save ProductImage (nếu có)
5. Trả `ProductResponse`

### 5.6. Deep dive: `ProductService`

**`getAll(ProductType, brandId, q, pageable)`**
- Input: filter `type`, `brandId`, `q`, `pageable`.
- Output: `Page<ProductResponse>`.
- Validation:
  - `q` trim nếu có.
- Side effects: none.

**`create(ProductCreateRequest)`**
- Input: DTO create.
- Output: `ProductResponse`.
- Validation:
  - `productCode` unique.
  - `brandId` active.
- Side effects:
  - Serialize `specsJson`.
  - Tạo `ProductImage` theo `imageUrls`.
- Edge cases:
  - Serialize lỗi → lưu `specsJson=null`.

**`update(Long, ProductUpdateRequest)`**
- Input: id + DTO update.
- Output: `ProductResponse`.
- Validation:
  - product phải active.
- Side effects:
  - Nếu `imageUrls` != null → delete ảnh cũ + tạo ảnh mới.

**`softDelete(Long)`**
- Input: id.
- Output: void.
- Side effects:
  - set `isDeleted=true`.

## 6) Services (Dịch vụ bảo trì)

### 6.1. Controller: `ServiceController`
- GET `/services`: `pageable` → `serviceService.getAll(pageable)`
- GET `/services/{id}`: `id` → `serviceService.getById(id)`
- POST `/services`: `ServiceCreateRequest` → `serviceService.create(request)`
- PUT `/services/{id}`: `id`, `ServiceUpdateRequest` → `serviceService.update(id, request)`
- DELETE `/services/{id}`: `id` → `serviceService.softDelete(id)`

### 6.2. Service: `ServiceService`
- CRUD dịch vụ, soft delete.

### 6.3. DTO/Entity/Repository
- DTO: `ServiceCreateRequest`, `ServiceUpdateRequest`, `ServiceResponse`.
- Entity: `Service`.
- Repository: `ServiceRepository`.

### 6.4. Input/Output/Validation/Side Effects

**POST `/services`**
- Input: `ServiceCreateRequest {serviceCode?, name, description?, basePrice, durationMinutes?}`
- Output: `ApiResponse<ServiceResponse>`
- Validation:
  - `name` not blank.
  - `basePrice >= 0`.
  - `durationMinutes >= 1` nếu có.
- Side effects:
  - Tạo `Service` (isDeleted=false).

**PUT `/services/{id}`**
- Input: `ServiceUpdateRequest` (field nullable)
- Output: `ApiResponse<ServiceResponse>`
- Side effects:
  - Update fields nếu có.

### 6.5. Sequence (Service create)
1. Client → `/services`
2. Validate input
3. Save Service
4. Trả `ServiceResponse`

### 6.6. Deep dive: `ServiceService`

**`getAll(Pageable)`**
- Input: pageable.
- Output: `Page<ServiceResponse>`.
- Side effects: none.

**`getById(Long)`**
- Input: id.
- Output: `ServiceResponse`.
- Validation:
  - Service phải active.

**`create(ServiceCreateRequest)`**
- Input: name, basePrice, durationMinutes, ...
- Output: `ServiceResponse`.
- Side effects:
  - Tạo `Service` (isDeleted=false).

**`update(Long, ServiceUpdateRequest)`**
- Input: id + DTO update.
- Output: `ServiceResponse`.
- Side effects:
  - Update field không null.

**`softDelete(Long)`**
- Input: id.
- Output: void.
- Side effects:
  - Set `isDeleted=true`.

## 7) Customers (Khách hàng)

### 7.1. Controller: `CustomerController`
- **GET `/customers/me`**: lấy hồ sơ của customer đang đăng nhập → `customerService.getMyProfile()`.
- **PUT `/customers/me`**: cập nhật hồ sơ của customer → `customerService.updateMyProfile(request)`.
- **GET `/customers`**: filter `q`, `pageable` → `customerService.getAll(q, pageable)`.
- **GET `/customers/{id}`**: `id` → `customerService.getById(id)`.
- **GET `/customers/lookup`**: `phone` → `customerService.findByPhone(phone)`.
- **POST `/customers`**: `CustomerCreateRequest` → `customerService.create(request)`.
- **PUT `/customers/{id}`**: `id`, `CustomerUpdateRequest` → `customerService.update(id, request)`.
- **GET `/customers/{id}/bookings`**: lịch bảo trì → `customerService.getBookings(id, pageable)`.

### 7.2. Service: `CustomerService`
- `getAll`: nếu role SALE → chỉ thấy khách do mình tạo; else all.
- `create`: có thể tạo `User` cho khách hàng nếu có username/password.
- `update`/`updateMyProfile`: validate trùng phone/email.
- `getMyProfile`: đọc `userId` từ JWT.
- `getBookings`: gọi `MaintenanceBookingRepository`.

### 7.3. DTO/Entity/Repository
- DTO: `CustomerCreateRequest`, `CustomerUpdateRequest`, `CustomerResponse`.
- Entity: `Customer`, `User`.
- Repository: `CustomerRepository`, `UserRepository`, `RoleRepository`, `MaintenanceBookingRepository`.

### 7.4. Input/Output/Validation/Side Effects

**POST `/customers`**
- Input: `CustomerCreateRequest` (fullName, phone, email?, address?, username?, password?, confirmPassword?)
- Output: `ApiResponse<CustomerResponse>`
- Validation:
  - `fullName`, `phone` bắt buộc.
  - `phone`/`email` không trùng.
  - Nếu có `username/password` → confirm password phải khớp.
- Side effects:
  - Có thể tạo `User` role CUSTOMER nếu có thông tin login.
  - `Customer.createdBy` = user tạo (Sale) hoặc chính user khách.

**GET `/customers/me`**
- Input: JWT access token
- Output: `ApiResponse<CustomerResponse>`
- Validation:
  - Có `Customer` theo `createdBy`.

### 7.5. Sequence (Customer create)
1. Client → `/customers`
2. Validate phone/email
3. Nếu có username/password → tạo User CUSTOMER
4. Lưu Customer
5. Trả `CustomerResponse`

### 7.6. Deep dive: `CustomerService`

**`getAll(String q, Pageable)`**
- Input: query text, pageable.
- Output: `Page<CustomerResponse>`.
- Validation:
  - Nếu role SALE → chỉ lấy khách do mình tạo.
- Side effects: none.

**`create(CustomerCreateRequest)`**
- Input: DTO create.
- Output: `CustomerResponse`.
- Validation:
  - `phone`/`email` không trùng.
  - Nếu có username/password → confirmPassword khớp.
- Side effects:
  - Có thể tạo `User` role CUSTOMER.
  - `Customer.createdBy` = user khách hoặc sale hiện tại.

**`update(Long, CustomerUpdateRequest)`**
- Input: id + DTO update.
- Output: `CustomerResponse`.
- Validation:
  - `phone`/`email` không trùng nếu đổi.
- Side effects:
  - Update các field.

**`getMyProfile()` / `updateMyProfile()`**
- Input: userId từ JWT.
- Output: `CustomerResponse`.
- Validation:
  - Customer theo createdBy phải tồn tại.
- Side effects:
  - Update field nếu là `updateMyProfile`.

## 8) Maintenance Bookings (Lịch bảo trì)

### 8.1. Controller: `MaintenanceBookingController`
- GET `/maintenance-bookings`: filter `status`, `customerId`, `from`, `to`, `pageable`.
- GET `/maintenance-bookings/{id}`: `id` → `bookingService.getById(id)`.
- POST `/maintenance-bookings`: `BookingCreateRequest` → `bookingService.create(request)`.
- PATCH `/maintenance-bookings/{id}/assign`: `BookingAssignRequest` → `bookingService.assignTechnician(id, request)`.
- PATCH `/maintenance-bookings/{id}/complete`: `BookingCompleteRequest` (nullable) → `bookingService.complete(id, req)`.
- PATCH `/maintenance-bookings/{id}/cancel`: `bookingService.cancel(id)`.
- PATCH `/maintenance-bookings/{id}/notes`: body `{notes}` → `bookingService.updateNotes(id, ...)`.
- PATCH `/maintenance-bookings/{id}/payment-status`: body `{paymentStatus}` → `bookingService.updatePaymentStatus(id, status)`.

### 8.2. Service: `MaintenanceBookingService`
- `create`: tìm/tạo Customer (theo customerId hoặc phone), tạo booking code, set status PENDING, tạo Payment nếu FREE/CASH.
- `createGuest`: giống create, nhưng cho khách vãng lai.
- `assignTechnician`: PENDING/CONFIRMED → CONFIRMED + gán technician.
- `complete`: CONFIRMED → COMPLETED; check quyền (admin hoặc technician được gán).
- `cancel`: PENDING/CONFIRMED → CANCELLED; customer chỉ hủy lịch của mình.
- `updateNotes`: update notes.
- `updatePaymentStatus`: tạo/ cập nhật Payment.

### 8.3. DTO/Entity/Repository
- DTO: `BookingCreateRequest`, `BookingAssignRequest`, `BookingCompleteRequest`, `BookingResponse`, `GuestBookingCreateRequest`.
- Entity: `MaintenanceBooking`, `Customer`, `Service`, `User`, `Payment`.
- Repository: `MaintenanceBookingRepository`, `CustomerRepository`, `ServiceRepository`, `UserRepository`, `PaymentRepository`.

### 8.4. Input/Output/Validation/Side Effects (module khó)

**POST `/maintenance-bookings`**
- Input: `BookingCreateRequest` (customerId hoặc phone/fullName, serviceId, bookingDate, paymentMethod, notes)
- Output: `ApiResponse<BookingResponse>`
- Validation:
  - `bookingDate` phải future.
  - Nếu không có `customerId` thì `phone` + `fullName` bắt buộc.
  - `serviceId` phải tồn tại và không bị xóa.
- Side effects:
  - Tạo hoặc cập nhật `Customer` theo phone.
  - Tạo `MaintenanceBooking` status PENDING + bookingCode.
  - Tạo `Payment` FREE hoặc CASH nếu cần.

**PATCH `/maintenance-bookings/{id}/assign`**
- Input: `BookingAssignRequest {technicianId}`
- Output: `ApiResponse<BookingResponse>`
- Validation:
  - Booking status phải PENDING/CONFIRMED.
  - Technician tồn tại.
- Side effects:
  - Gán technician, set status CONFIRMED.

**PATCH `/maintenance-bookings/{id}/complete`**
- Input: `BookingCompleteRequest {notes?}`
- Output: `ApiResponse<BookingResponse>`
- Validation:
  - Booking status phải CONFIRMED.
  - Nếu role TECHNICIAN → phải là technician được gán.
- Side effects:
  - Cập nhật notes (nếu có) + status COMPLETED.

### 8.5. Sequence (Booking create)
1. Client → `/maintenance-bookings` với request
2. Service tìm/tạo Customer
3. Service đọc Service theo `serviceId`
4. Tạo booking code → lưu Booking
5. Tạo Payment (FREE/CASH) nếu cần
6. Trả `BookingResponse`

### 8.6. Deep dive: `MaintenanceBookingService`

**`getAll(status, from, to, customerId, pageable)`**
- Input: filters + pageable.
- Output: `Page<BookingResponse>`.
- Validation:
  - Nếu role TECHNICIAN → chỉ trả lịch của chính mình.
- Side effects: none.

**`create(BookingCreateRequest)`**
- Input: DTO create.
- Output: `BookingResponse`.
- Validation:
  - `bookingDate` future.
  - `serviceId` tồn tại và active.
- Side effects:
  - Tạo/ cập nhật Customer theo phone.
  - Tạo Booking + Payment (FREE/CASH).

**`assignTechnician(bookingId, request)`**
- Input: bookingId, technicianId.
- Output: `BookingResponse`.
- Validation:
  - Status phải PENDING/CONFIRMED.
- Side effects:
  - Set technician + status CONFIRMED.

**`complete(bookingId, request)`**
- Input: bookingId, notes?
- Output: `BookingResponse`.
- Validation:
  - Status phải CONFIRMED.
  - Role TECHNICIAN phải đúng người được gán.
- Side effects:
  - Set status COMPLETED + notes.

**`cancel(bookingId)`**
- Input: bookingId.
- Output: `BookingResponse`.
- Validation:
  - Role CUSTOMER chỉ hủy booking của mình.
- Side effects:
  - Set status CANCELLED.

## 9) Orders (Đơn hàng)

### 9.1. Controller: `OrderController`
- GET `/orders`: filter `status`, `customerId`, `from`, `to`, `pageable`.
- GET `/orders/{id}`: `orderService.getById(id)`.
- POST `/orders`: `OrderCreateRequest` → `orderService.create(request)`.
- PATCH `/orders/{id}/status`: `OrderStatusUpdateRequest` → `orderService.updateStatus(id, request)`.
- PATCH `/orders/{id}/notes`: body `{notes}` → `orderService.updateNotes(id, ...)`.
- PATCH `/orders/{id}/payment-status`: body `{paymentStatus}` → `orderService.updatePaymentStatus(id, status)`.

### 9.2. Service: `OrderService`
- `create`: tìm/tạo Customer, validate tồn kho, tạo orderCode, tạo `OrderItem`, tính tổng, tạo Payment.
- `createGuest`: tương tự, sale = null.
- `updateStatus`: state machine (PENDING→PROCESSING/CANCELLED, PROCESSING→COMPLETED/CANCELLED). Khi COMPLETED → trừ tồn kho.
- `updateNotes`: cập nhật ghi chú.
- `updatePaymentStatus`: tạo/cập nhật Payment.

### 9.3. DTO/Entity/Repository
- DTO: `OrderCreateRequest`, `OrderItemRequest`, `OrderStatusUpdateRequest`, `OrderResponse`, `OrderItemResponse`, `GuestOrderCreateRequest`.
- Entity: `Order`, `OrderItem`, `Product`, `Customer`, `Payment`.
- Repository: `OrderRepository`, `OrderItemRepository`, `ProductRepository`, `CustomerRepository`, `PaymentRepository`.

### 9.4. Input/Output/Validation/Side Effects (module khó)

**POST `/orders`**
- Input: `OrderCreateRequest` (customerId hoặc phone/fullName, items, shippingAddress, paymentMethod, notes)
- Output: `ApiResponse<OrderResponse>`
- Validation:
  - `items` not empty, mỗi item có `productId`, `quantity>=1`.
  - Nếu không có `customerId` thì `phone` + `fullName` bắt buộc.
  - Kiểm tra tồn kho trước khi tạo.
- Side effects:
  - Tạo hoặc cập nhật `Customer`.
  - Tạo `Order` + `OrderItem`, snapshot giá.
  - Tạo `Payment` FREE/CASH nếu cần.

**PATCH `/orders/{id}/status`**
- Input: `OrderStatusUpdateRequest {status}`
- Output: `ApiResponse<OrderResponse>`
- Validation:
  - State machine: PENDING → PROCESSING/CANCELLED; PROCESSING → COMPLETED/CANCELLED.
  - Không chuyển trạng thái từ COMPLETED/CANCELLED.
- Side effects:
  - Nếu COMPLETED → trừ tồn kho `Product.stockQuantity`.

### 9.5. Sequence (Order create)
1. Client → `/orders`
2. Service tìm/tạo Customer theo phone/customerId
3. Validate tồn kho cho từng item
4. Tạo Order + OrderItem, tính total
5. Tạo Payment nếu FREE/CASH
6. Trả `OrderResponse`

### 9.6. Deep dive: `OrderService`

**`getAll(status, customerId, from, to, pageable)`**
- Input: filters + pageable.
- Output: `Page<OrderResponse>`.
- Validation:
  - Role SALE chỉ xem đơn của mình.
- Side effects: none.

**`create(OrderCreateRequest)`**
- Input: DTO create.
- Output: `OrderResponse`.
- Validation:
  - `items` not empty, tồn kho đủ.
- Side effects:
  - Tạo Order + OrderItem.
  - Tạo Payment (FREE/CASH).

**`updateStatus(id, OrderStatusUpdateRequest)`**
- Input: id + status.
- Output: `OrderResponse`.
- Validation:
  - State machine hợp lệ.
- Side effects:
  - Nếu COMPLETED → trừ tồn kho.

**`updatePaymentStatus(id, newStatus)`**
- Input: id + PaymentStatus.
- Output: `OrderResponse`.
- Side effects:
  - Tạo Payment record nếu chưa có.

## 10) PayPal (Thanh toán)

### 10.1. Controller: `PaypalController`
- POST `/paypal/create`: `PaypalCreateRequest` → `paymentService.createPaypalPayment(request)`.
- POST `/paypal/execute`: `paymentId`, `PayerID` → `paymentService.executePaypalPayment(...)`.

### 10.2. Service: `PaymentService` + `PaypalService`
- `PaymentService.createPaypalPayment`: định tuyến theo `referenceType` (ORDER/BOOKING).
- `createOrderPayment` / `createBookingPayment`:
  - Tính số tiền VND → USD.
  - Tạo PayPal payment (SDK) → lấy approvalUrl.
  - Lưu `Payment` với status CREATED.
- `executePaypalPayment`:
  - Idempotent: nếu đã APPROVED thì trả kết quả ngay.
  - Nếu PayPal approved → cập nhật status APPROVED, cập nhật Order nếu cần.

### 10.3. DTO/Entity/Repository
- DTO: `PaypalCreateRequest`, `PaypalCreateResponse`, `PaypalExecuteResponse`.
- Entity: `Payment`, `Order`, `MaintenanceBooking`.
- Repository: `PaymentRepository`, `OrderRepository`, `MaintenanceBookingRepository`.

### 10.4. Input/Output/Validation/Side Effects (module khó)

**POST `/paypal/create`**
- Input: `PaypalCreateRequest {referenceType, referenceId}`
- Output: `ApiResponse<PaypalCreateResponse>` (có `approvalUrl`)
- Validation:
  - `referenceId` tồn tại (Order/Booking).
  - Amount > 0 với flow PAYPAL (nếu FREE → trả status FREE).
- Side effects:
  - Gọi PayPal SDK tạo payment.
  - Lưu `Payment` status CREATED + approvalUrl.

**POST `/paypal/execute`**
- Input: `paymentId`, `PayerID`
- Output: `ApiResponse<PaypalExecuteResponse>`
- Validation:
  - `paymentId` tồn tại.
  - Idempotent: nếu đã APPROVED → trả OK.
- Side effects:
  - Cập nhật `Payment.status`.
  - Với Order: nếu status PENDING → chuyển PROCESSING.

### 10.5. Sequence (PayPal execute)
1. Client redirect về FE → gọi `/paypal/execute`
2. Service gọi PayPal SDK execute
3. Cập nhật `Payment` + (Order nếu cần)
4. Trả `PaypalExecuteResponse`

### 10.6. Deep dive: `PaymentService`

**`createPaypalPayment(PaypalCreateRequest)`**
- Input: `referenceType`, `referenceId`.
- Output: `PaypalCreateResponse`.
- Validation:
  - Order/Booking tồn tại.
- Side effects:
  - Tạo `Payment` record với `approvalUrl`.

**`executePaypalPayment(paymentId, payerId)`**
- Input: PayPal IDs.
- Output: `PaypalExecuteResponse`.
- Validation:
  - Nếu đã APPROVED → trả idempotent.
- Side effects:
  - Update `Payment.status`, update Order nếu cần.

## 11) Guest (Khách vãng lai)

### 11.1. Controller: `GuestController`
- POST `/guest/orders`: `GuestOrderCreateRequest` → `orderService.createGuest(request)`.
- POST `/guest/bookings`: `GuestBookingCreateRequest` → `bookingService.createGuest(request)`.

### 11.2. Input/Output/Validation/Side Effects

**POST `/guest/orders`**
- Input: `GuestOrderCreateRequest` (fullName, phone, shippingAddress, items, paymentMethod?, notes?)
- Output: `ApiResponse<OrderResponse>`
- Validation:
  - `phone` format, `items` not empty.
- Side effects:
  - Tạo Customer nếu chưa có (sale=null).
  - Tạo Order + Payment.

**POST `/guest/bookings`**
- Input: `GuestBookingCreateRequest` (fullName, phone, address?, serviceId, bookingDate)
- Output: `ApiResponse<BookingResponse>`
- Validation:
  - `bookingDate` future.
- Side effects:
  - Tạo Customer nếu chưa có.
  - Tạo Booking + Payment.

## 12) Chat (REST + WebSocket)

### 12.1. REST Controller: `ChatController`
- GET `/chat/my-conversation`: lấy conversation của customer (JWT).
- GET `/chat/conversations`: admin list conversation.
- GET `/chat/conversations/{id}/messages`: history.
- POST `/chat/conversations/{id}/takeover`: admin nhận conversation.
- POST `/chat/conversations/{id}/close`: đóng conversation.
- GET `/chat/online-admins`: danh sách admin online.

### 12.2. WebSocket Controller: `ChatWebSocketController`
- `chat.customer.send`: customer gửi message.
- `chat.admin.send`: admin gửi message.
- `chat.typing.{conversationId}`: typing event.
- `chat.seen.{conversationId}`: seen event.
- `chat.admin.online` / `chat.admin.offline`.

### 12.3. Service: `ChatService`
- Quản lý conversation, auto-reply, notify admin.
- `customerSendMessage`/`adminSendMessage`: lưu `ChatMessage`, cập nhật unread count, broadcast.
- `autoAssignAdmin`: gán admin ít tải nhất.
- `takeoverConversation`, `closeConversation`, `markAsSeen`.

### 12.4. DTO/Entity/Repository
- DTO: `ChatMessageRequest`, `ChatMessageResponse`, `ConversationResponse`.
- Entity: `Conversation`, `ChatMessage`, `User`, `Customer`.
- Repository: `ConversationRepository`, `ChatMessageRepository`, `CustomerRepository`, `UserRepository`.

### 12.5. Input/Output/Validation/Side Effects (module khó)

**WS `chat.customer.send`**
- Input: `ChatMessageRequest {conversationId?, content}`
- Output: broadcast `ChatMessageResponse` đến `/topic/chat/{conversationId}`
- Validation:
  - `content` not blank, max 2000.
- Side effects:
  - Tạo Conversation nếu chưa có.
  - Lưu `ChatMessage`.
  - Update unread count.
  - Auto-reply + notify admins nếu conversation mới.

**WS `chat.admin.send`**
- Input: `ChatMessageRequest {conversationId, content}`
- Output: broadcast đến `/topic/chat/{conversationId}` và `/topic/customer/{id}/message`
- Side effects:
  - Lưu message, update unread.

**REST `/chat/conversations/{id}/takeover`**
- Input: `id` (path)
- Output: `ApiResponse<ConversationResponse>`
- Validation:
  - Conversation tồn tại.
- Side effects:
  - Gán admin, status ACTIVE, gửi system message.

### 12.6. Sequence (Customer send message)
1. WS client → `chat.customer.send`
2. Service tìm/ tạo conversation
3. Lưu message, update conversation, broadcast
4. Nếu conversation mới → auto-reply + notify admin

### 12.7. Deep dive: `ChatService`

**`customerSendMessage(customerId, request)`**
- Input: customerId, `ChatMessageRequest`.
- Output: `ChatMessageResponse`.
- Validation:
  - Customer user tồn tại.
  - content not blank.
- Side effects:
  - Tạo conversation nếu chưa có (WAITING).
  - Lưu message, update unreadCountAdmin.
  - Broadcast message + notify admins.

**`adminSendMessage(adminId, request)`**
- Input: adminId, conversationId.
- Output: `ChatMessageResponse`.
- Side effects:
  - Update unreadCountCustomer.
  - Broadcast to conversation + customer.

**`takeoverConversation(adminId, conversationId)`**
- Input: adminId, conversationId.
- Output: `ConversationResponse`.
- Side effects:
  - Set assignedAdmin + status ACTIVE.
  - Send system message.

**`markAsSeen(conversationId, readerId)`**
- Input: conversationId, readerId.
- Output: void.
- Side effects:
  - Update `ChatMessage.seen` + reset unread count.

## 13) File Upload

### 13.1. Controller: `FileUploadController`
- POST `/files/upload`: nhận `MultipartFile file`, gọi `fileStorageService.store(file)`, trả URL.

### 13.2. Service: `FileStorageService`
- Validate mime type + size (5MB), generate filename, save file vào `uploads`.

### 13.3. Input/Output/Validation/Side Effects

**POST `/files/upload`**
- Input: `MultipartFile file`
- Output: `ApiResponse<String>` (URL của file)
- Validation:
  - File không rỗng, size <= 5MB.
  - Content-Type thuộc JPEG/PNG/WebP/GIF.
- Side effects:
  - Tạo file vật lý trong thư mục upload.

### 13.4. Deep dive: `FileStorageService`

**`store(MultipartFile)`**
- Input: file.
- Output: `filename` (UUID + extension).
- Validation:
  - File không rỗng.
  - Size <= 5MB.
  - Mime type nằm trong whitelist.
- Side effects:
  - Ghi file vào thư mục upload.

## 14) Ghi chú về biến và nơi sử dụng

Trong mỗi Controller/Service:
- **`request`** (DTO) luôn được dùng để đọc input field (file: DTO tương ứng).
- **`id`** (PathVariable) → dùng để truy vấn entity (`findById`, `findByIdAndIsDeletedFalse`).
- **`pageable`** (Spring Data) → chuyển xuống Service/Repository để phân trang.
- **`jwt` / `SecurityContextHolder`** → lấy `userId`, `scope` (file: `AuthService`, `CustomerService`, `OrderService`, `MaintenanceBookingService`, `ChatController`).
- **`body` (Map)** → dùng cho update nhanh (notes/paymentStatus) trong Order/Booking controller.

---

Nếu bạn muốn tôi xuất thêm sơ đồ sequence hoặc mô tả chi tiết từng method theo từng class (đi sâu hơn từng biến), hãy nói rõ mục ưu tiên (module nào trước).