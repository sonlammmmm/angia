# KỂ HOẠCH PHÁT TRIỂN BACKEND ANGIA - 12 TUẦN

**Ngày tạo:** Tháng 5, 2026  
**Dự án:** Hệ thống Quản lý Dịch vụ & Bán hàng - Máy lọc nước An Gia  
**Tech Stack:** Spring Boot 3.5.11 + Java 25 + MySQL 8.x  
**Độ phức tạp:** Cao (State machines, Transactions, Role-based Access Control, Payment integration)

---

## 📋 TỔNG QUAN DỰ ÁN

### Mục tiêu chính
Xây dựng backend REST API hoàn chỉnh cho hệ thống quản lý dịch vụ & bán hàng máy lọc nước, với trọng tâm là **luồng bảo trì định kỳ** (maintenance booking).

### Phạm vi công việc
- ✅ 11 entities (Database schema)
- ✅ Authentication & JWT security
- ✅ Role-based access control (4 roles)
- ✅ Order management with inventory tracking
- ✅ Maintenance booking (core business)
- ✅ PayPal payment integration
- ✅ Swagger API documentation
- ✅ Security hardening (fix critical vulnerabilities)
- ✅ Unit testing

### Điểm khó & rủi ro
| Khó khăn | Impact | Giải pháp |
|---|---|---|
| State machines (Orders, Bookings) | High | Implement strict validation từ bước 2 |
| Transactional integrity (inventory) | High | `@Transactional(rollbackFor=Exception.class)` everywhere |
| Data-level authorization (Sale/Tech) | High | Implement filtering ở Service layer, test early |
| JWT + Spring Security OAuth2 + Nimbus | Medium | Ref: `04_SECURITY.md` - don't mix JWT libraries |
| PayPal VND→USD conversion | Medium | Third-party rate API hoặc ECB |

---

## 📅 LỊCH TRÌNH CHI TIẾT

### **TUẦN 1-2: PHẦN NỀN TẢNG (Foundation)**

#### Mục tiêu
- Xây dựng infrastructure chung (Exception handling, Response wrapper, Enums)
- Setup database + Entity mapping
- CORS & basic security config

#### Công việc cụ thể

**Tuần 1**
- [ ] **1.1** Phân tích & approve architecture (check CONTROLLER_FLOW.md, OWASP audit)
- [ ] **1.2** Update `pom.xml`: add JWT + Security + Swagger + PayPal dependencies
  - `spring-boot-starter-oauth2-resource-server`
  - `spring-boot-starter-security`
  - `springdoc-openapi-starter-webmvc-ui:2.8.8`
  - `com.paypal.sdk:rest-api-sdk:1.14.0`
- [ ] **1.3** Create `vn.dichvuangia.management.common/ApiResponse.java`
  - Generic response wrapper with `@Getter @Builder`
  - Fields: `status`, `message`, `data`, `errorCode`
  - 3 factory methods: `success(data)`, `success(msg, data)`, `error(msg, code)`
- [ ] **1.4** Create 3 enums in `vn.dichvuangia.management.common.enums/`
  - `ProductType {MACHINE, FILTER}`
  - `OrderStatus {PENDING, PROCESSING, COMPLETED, CANCELLED}`
  - `BookingStatus {PENDING, CONFIRMED, COMPLETED, CANCELLED}`
- [ ] **1.5** Create custom exceptions in `exception/`
  - `ResourceNotFoundException(String resource, Long id)`
  - `InsufficientStockException(String productName, int available)`
  - `InvalidStatusTransitionException(String from, String to)`
  - `BookingAlreadyCompletedException(Long bookingId)`
  - All extend `RuntimeException`

**Tuần 2**
- [ ] **2.1** Create `GlobalExceptionHandler.java`
  - `@RestControllerAdvice @Slf4j`
  - Handle 8 custom + 4 Spring exceptions
  - Rule: Log stacktrace internally, **never expose to client**
- [ ] **2.2** Create `CorsConfig.java`
  - Read `app.cors.allowed-origins` from properties
  - `allowCredentials = true` (for Refresh Token cookies)
- [ ] **2.3** Create `SecurityConfig.java` skeleton (to be completed in week 3)
- [ ] **2.4** Create all 11 JPA Entities + Repositories
  - Follow dependency order in rules/05_CODING_STEPS.md §BƯỚC 2
  - All entities: `@PrePersist/@PreUpdate` for timestamps, `FetchType.LAZY`
  - Create corresponding `*Repository` interfaces
- [ ] **2.5** Database setup: Create MySQL schema (11 tables)
  - Import SQL script or verify Hibernate auto-creation
  - Test connection with query
- [ ] **2.6** CI/Deploy: Setup build pipeline (.gitignore, Maven build)
  - Verify `mvn clean package` compiles without error

**Deliverable Week 1-2:** Database ready + all custom exceptions + Entity layer complete

**Estimation:** 4 developers × 2 weeks, 8 person-weeks

---

### **TUẦN 3-4: XÁC THỰC & BẢO MẬT (Authentication & Security)**

#### Mục tiêu
- Implement JWT + Spring Security OAuth2 Resource Server (Nimbus)
- User authentication (login, refresh token, logout)
- Fix critical security vulnerabilities from audit

#### Công việc cụ thể

**Tuần 3**
- [ ] **3.1** Fix security audit findings:
  - [ ] Move DB password → environment variables (use `${spring.datasource.password}`)
  - [ ] Move JWT secret → environment variables (use `${app.jwt.secret}`)
  - [ ] Apply `app.cors.allowed-origins` from properties (already in CorsConfig)
  - [ ] Cookie: set `secure=true` when deploy to HTTPS
- [ ] **3.2** Create `JwtService.java`
  - `generateAccessToken(User user)`: claims = userId, scope(role)
  - `generateRefreshToken()`: UUID, TTL = 7d, persist to DB
  - Use `JwtEncoder` (Nimbus), expiration from properties
- [ ] **3.3** Create `UserDetailsServiceImpl.java`
  - Implement `UserDetailsService.loadUserByUsername(String)`
  - Check `user.isActive()` → set `enabled` on UserDetails
  - If `is_active=false`, throw `DisabledException` (Spring handles → 403)
- [ ] **3.4** Complete `SecurityConfig.java`
  - `JwtDecoder` bean (HMAC-SHA256)
  - `JwtEncoder` bean
  - `SecurityFilterChain` with authorization rules (8 patterns, see rules/04_SECURITY.md)
  - CORS bean configuration
- [ ] **3.5** Update `GlobalExceptionHandler`
  - Add handlers for: `DisabledException`, `AccessDeniedException`, `JwtException`
  - Map to: HTTP 403 ACCOUNT_DISABLED, ACCESS_DENIED, UNAUTHORIZED

**Tuần 4**
- [ ] **4.1** Create `AuthService.java`
  - `login(LoginRequest)`: authenticate → generate tokens → return AccessToken
  - `refresh(refreshTokenString)`: validate token in DB → generate new pair → rotate (delete old)
  - `logout(refreshTokenString)`: delete from `refresh_tokens` table
- [ ] **4.2** Create `AuthController.java`
  - POST `/auth/login` → public
  - POST `/auth/refresh` → read from Cookie
  - POST `/auth/logout` → delete cookie (maxAge=0)
  - All return `ApiResponse<AuthResponse>` with access token in body, refresh in HttpOnly cookie
  - Add Swagger `@Operation` + `@ApiResponses`
- [ ] **4.3** Create `UserDetailsMapper` (optional, for clarity)
  - Map `User` entity ↔ `UserDetails`
- [ ] **4.4** Create test users (3 roles: ADMIN, SALE, TECHNICIAN)
  - INSERT dummy users in test database setup
  - Document default credentials for testing
- [ ] **4.5** Test authentication flow end-to-end
  - Login → get AccessToken
  - Use AccessToken in Authorization header
  - Refresh → get new tokens
  - Logout → token invalid on next request
  - Disabled account → 403

**Deliverable Week 3-4:** Complete JWT flow, test login/logout

**Estimation:** 2 developers × 2 weeks, 4 person-weeks

---

### **TUẦN 5-6: QUẢN LÝ SẢN PHẨM & HÃN SX (Product & Brand Management)**

#### Mục tiêu
- CRUD endpoints for Products & Brands
- Inventory tracking foundation
- Soft delete pattern implementation

#### Công việc cụ thể

**Tuần 5**
- [ ] **5.1** Create DTOs in `dto/request` + `dto/response`
  - `ProductCreateRequest, ProductUpdateRequest, ProductResponse`
  - `BrandCreateRequest, BrandUpdateRequest, BrandResponse`
- [ ] **5.2** Create `BrandService.java`
  - `getAll(Pageable)`: filter `is_deleted=false`
  - `getById(id)`, `create()`, `update()`
  - `softDelete(id)`: set `is_deleted=true`
- [ ] **5.3** Create `ProductService.java`
  - `getAll(productType, brandId, pageable)`: filter + soft delete
  - `getById(id)`, `create()`
  - `update()`: validate no stock changes if `stock_quantity` is reducing (risky)
  - `softDelete(id)`: set `is_deleted=true`
  - Handle JSON serialization for `specs_json` (use `ObjectMapper` or converter)
- [ ] **5.4** Create `BrandController.java`
  - GET `/brands` (public)
  - POST, PUT, DELETE `/brands/**` (ADMIN, MANAGEMENT, ADMIN)
  - Swagger docs
- [ ] **5.5** Create `ProductController.java`
  - GET `/products` + `/products/{id}` (public)
  - POST, PUT, DELETE `/products/**` (ADMIN, MANAGEMENT, ADMIN)
  - Query params: `type`, `brandId`, `page`, `size`, `sort`
  - Swagger docs

**Tuần 6**
- [ ] **6.1** Create `ProductImageService.java` & `Controller`
  - POST `/products/{id}/images`: receive image file
  - Store in `uploads/product-images/` folder
  - Save URL to `product_images` table
  - Mark main image: `PATCH /products/{id}/images/{imageId}/set-main`
- [ ] **6.2** Pagination & sorting tests
  - Test `page=0&size=10&sort=price,desc`
  - Verify response counts
- [ ] **6.3** Input validation
  - `@Valid` on all request DTOs
  - Test invalid inputs → 400 + error message
  - Price validation: must > 0, decimal(12,2)
- [ ] **6.4** Query optimization
  - Add `@Query` for complex filtering if needed
  - Verify no N+1 queries (use `@EntityGraph` or projection)
- [ ] **6.5** Integration tests
  - Test product creation → read → update → soft delete flow
  - Verify soft deleted products don't appear in list
  - Test brand → products filtering

**Deliverable Week 5-6:** Full product/brand CRUD with soft delete, image upload

**Estimation:** 2 developers × 2 weeks, 4 person-weeks

**⚠️ Critical check before moving to Week 7:**
- [ ] All API tests green
- [ ] Swagger UI shows all endpoints
- [ ] Curl can access: `GET /api/v1/products?page=0&size=10`

---

### **TUẦN 7-8: QUẢN LÝ KHÁCH & ĐƠN HÀNG (Customer & Order Management)**

#### Mục tiêu
- Customer CRM endpoints
- Order creation with inventory validation
- Order status state machine with transaction safety

#### Công việc cụ thể

**Tuần 7**
- [ ] **7.1** Create `CustomerService.java`
  - `getAll()`: paginated, with data-level auth (SALE sees own customers only)
  - `getById(id)`, `create(customerId, phoneNumber, address)`
  - `update(id, ...)`, `delete()` (soft or hard - follow design)
  - Track `created_by` (who created this customer)
- [ ] **7.2** Create `CustomerController.java`
  - GET `/customers` + `/customers/{id}` (all authenticated)
  - POST `/customers` (ADMIN, SALE)
  - PUT `/customers/{id}` (ADMIN, SALE - own only)
  - GET `/customers/{id}/bookings` (fetch related bookings)
- [ ] **7.3** Create DTOs for Customer
  - `CustomerCreateRequest, UpdateRequest, Response`
- [ ] **7.4** Create `OrderService.java` - **Core complexity here**
  - `create(OrderCreateRequest)`:
    - Validate all products exist
    - Check stock for each item (throw `InsufficientStockException` if not enough)
    - Create Order (status=PENDING) + OrderItems
    - Calculate total_amount
    - Return `OrderResponse`
  - Structure: should be `@Transactional(rollbackFor=Exception.class)`
- [ ] **7.5** Create `updateOrderStatus(orderId, newStatus)` - **Most critical method**
  - Validate state machine transition (see rules/02_BUSINESS_LOGIC.md §2)
  - If newStatus = COMPLETED:
    - Loop each OrderItem → decrement Product.stock_quantity
    - If any product insufficient stock → throw exception → **entire method rolls back**
    - `@Transactional(rollbackFor=Exception.class)` ensures atomicity
  - If newStatus = CANCELLED (from COMPLETED):
    - Increment stock back for all items
  - Save order with new status

**Tuần 8**
- [ ] **8.1** Create `OrderController.java`
  - GET `/orders` + `/orders/{id}` (with data-level filtering)
  - POST `/orders` (ADMIN, SALE)
  - PATCH `/orders/{id}/status` (ADMIN, MANAGEMENT)
  - Include order items in response
- [ ] **8.2** Create DTOs for Order
  - `OrderCreateRequest` (customerId, items[], shippingAddress)
  - `OrderItemRequest` (productId, quantity)
  - `OrderUpdateStatusRequest` (status)
  - `OrderResponse` (full details)
- [ ] **8.3** Data-level authorization for Orders
  - In `OrderService.getAll()`: if user is SALE, filter `sale_id = currentUserId`
  - Implement `getCurrentUserId()` helper extracting from JWT SecurityContext
- [ ] **8.4** Comprehensive transaction testing
  - **Test 1:** Create order with 3 items, complete → stock decreases for all 3
  - **Test 2:** Create order, product has insufficient stock on complete → 400 error + no stock change
  - **Test 3:** Create 2 orders, complete first → stock OK, second → insufficient → second fails atomically
  - **Test 4:** Complete → CANCELLED → stock restored
- [ ] **8.5** Integration with Products
  - Verify when order completes, can't create another order for deleted product
  - Soft delete products should prevent new orders
- [ ] **8.6** API documentation
  - Add Swagger examples for each endpoint
  - Document error scenarios (404, 400, 409)

**Deliverable Week 7-8:** Full order flow with transactional integrity, tested

**Estimation:** 3 developers × 2 weeks, 6 person-weeks

**🔴 CRITICAL MILESTONE CHECK:**
- [ ] Transactional test passes (inventory doesn't go negative)
- [ ] State machine validated (invalid transitions rejected)
- [ ] Soft delete + order flow works (no ghost orders for deleted products)

---

### **TUẦN 9-10: ĐẶT LỊCH BẢO TRÌ — LÕI NGHIỆP VỤ (Maintenance Booking — Core Business)**

#### Mục tiêu
- Implement 3-step maintenance booking workflow
- Role-based data access (Technician sees own bookings only)
- Query filtering by date range, status, person

#### Công việc cụ thể

**Tuần 9**
- [ ] **9.1** Create `ServiceService.java`
  - CRUD for maintenance services
  - Soft delete support
- [ ] **9.2** Create `MaintenanceBookingService.java` - **Most critical**
  - `create(BookingCreateRequest)`:
    - Validate customer exists, service exists
    - Create booking with status=PENDING, technician_id=NULL
    - Return `BookingResponse`
  - `assignTechnician(bookingId, technicianId)`:
    - Validate PENDING → CONFIRMED transition
    - Validate technician exists + is TECHNICIAN role
    - Update booking, set technician_id, status=CONFIRMED
    - `@Transactional`
  - `completeBooking(bookingId, notes)`:
    - Validate CONFIRMED → COMPLETED transition
    - Verify current user = assigned technician (or ADMIN)
    - Set notes, status=COMPLETED
    - `@Transactional`
  - `cancelBooking(bookingId)`:
    - Allow from PENDING or CONFIRMED
    - Update status=CANCELLED
    - `@Transactional`
  - `getAll(filters, pageable)`:
    - Support filters: `status`, `customerId`, `technicianId`, `fromDate`, `toDate`
    - Data-level auth: TECHNICIAN sees `technician_id=currentUserId` only
- [ ] **9.3** Data-level authorization helper
  - `getCurrentUserId()` from JWT
  - `getCurrentUserRole()` from JWT scope
  - Reuse in all Services

**Tuần 10**
- [ ] **10.1** Create `MaintenanceBookingController.java`
  - GET `/maintenance-bookings` (all authenticated, filtered by role)
  - GET `/maintenance-bookings/{id}`
  - POST `/maintenance-bookings` (ADMIN, MANAGEMENT, SALE)
  - PATCH `/maintenance-bookings/{id}/assign` (ADMIN, MANAGEMENT)
  - PATCH `/maintenance-bookings/{id}/complete` (TECHNICIAN, ADMIN)
  - PATCH `/maintenance-bookings/{id}/cancel` (ADMIN, MANAGEMENT)
  - All with Swagger docs
- [ ] **10.2** Create DTOs for Booking
  - `BookingCreateRequest` (customerId, serviceId, bookingDate, notes)
  - `BookingAssignRequest` (technicianId)
  - `BookingCompleteRequest` (notes)
  - `BookingResponse` (nested: customer, service, technician)
- [ ] **10.3** Test authorization flows
  - **Test 1:** SALE creates booking → sees it
  - **Test 2:** TECHNICIAN tries to assign → 403 (insufficient permission)
  - **Test 3:** MANAGEMENT assigns tech → booking CONFIRMED
  - **Test 4:** Wrong technician tries complete → 403 (not assigned to this booking)
  - **Test 5:** Assigned technician completes → 200
  - **Test 6:** TECHNICIAN queries all bookings → only sees assigned ones
- [ ] **10.4** Test state machine
  - PENDING ↔ all transitions tested
  - Invalid transitions → 400
  - Completed booking can't transition back
- [ ] **10.5** Test date range filtering
  - Query bookings between `from=2025-08-01&to=2025-08-31`
  - Verify results include only bookings in range
- [ ] **10.6** Integration with Orders & Customers
  - Link customer to bookings in response
  - GET `/customers/{id}/bookings` returns list
  - Verify booking history

**Deliverable Week 9-10:** Full maintenance booking CRUD with state machine + role-based filtering

**Estimation:** 3 developers × 2 weeks, 6 person-weeks

**✅ MAJOR MILESTONE — Maintenance Booking Complete:**
- [ ] All 3 booking endpoints tested
- [ ] Authorization rules enforced
- [ ] State machine solid (no invalid transitions)
- [ ] Data-level filtering verified (Tech only sees own bookings)

---

### **TUẦN 11: TÍCH HỢP THANH TOÁN PAYPAL & KIỂM THỬ (PayPal Integration & Testing)**

#### Mục tiêu
- Integrate PayPal SDK for order checkout
- Handle VND → USD conversion
- End-to-end payment flow testing

#### Công việc cụ thể

**Tuần 11a (3 ngày)**
- [ ] **11.1** Add PayPal config
  - Create `PaypalConfig.java` with `APIContext` bean
  - Properties: `paypal.mode` (sandbox/live), `paypal.client.id`, `paypal.client.secret`
  - Store credentials in environment variables (not hardcoded)
- [ ] **11.2** Create `PaypalService.java`
  - `createPayment(amount, currency, returnUrl, cancelUrl)`:
    - Convert VND → USD if needed (fetch rate from API or use fixed)
    - Create PayPal `Payment` object
    - Return approval URL
  - `executePayment(paymentId, payerId)`:
    - Verify & execute payment
    - Return transaction ID
  - Error handling for all PayPal exceptions
- [ ] **11.3** Create `PaymentController.java`
  - POST `/payments/create` (ADMIN, SALE) → return PayPal approval URL
  - GET `/payments/execute?paymentId={id}&payerId={id}` (public) → execute payment
  - POST `/payments/callback` (webhook) → update order status on successful payment
  - Handle payment failures

**Tuần 11b (2 ngày)**
- [ ] **11.4** Update `Order` entity & `OrderService`
  - Add `paymentStatus` field (PENDING, PAID, FAILED)
  - Add `transactionId` field (PayPal transaction ID)
  - When order status = COMPLETED and payment executed → update payment status
- [ ] **11.5** End-to-end testing
  - **Test 1:** Create order → generate PayPal payment URL → verify format
  - **Test 2:** Execute payment with mock PayPal response → order status updated
  - **Test 3:** Failed payment → order remains status=PENDING
  - **Test 4:** Currency conversion (1,000,000 VND → ~40 USD) verified
- [ ] **11.6** Documentation
  - Update Swagger API docs with payment endpoints
  - Add PayPal flow diagram to docs
  - Document credentials setup for sandbox/production

**Deliverable Week 11:** PayPal integration complete + tested

**Estimation:** 1-2 developers × 1 week, 1-2 person-weeks

---

### **TUẦN 12: KIỂM THỬ TOÀN DIỆN, HARDENING & LỮA HÀNH (Full Testing, Security Hardening & Deployment)**

#### Mục tiêu
- Complete security audit fixes
- Comprehensive unit + integration tests
- API documentation finalization
- Deployment readiness

#### Công việc cụ thể

**Tuần 12a (2 ngày) — Testing**
- [ ] **12.1** Unit tests (target: 70%+ coverage)
  - `*Service` tests: mock repositories, test business logic
  - `*Controller` tests: test endpoints + authorization
  - Exception handling tests
- [ ] **12.2** Integration tests
  - Database integration: test against real MySQL
  - End-to-end flows: login → order → booking → payment
  - Verify transactions work correctly
- [ ] **12.3** Authorization testing (critical)
  - Test each endpoint with all 4 roles
  - Verify 403 for unauthorized access
  - Verify data-level filtering (SALE, TECHNICIAN)
- [ ] **12.4** Edge case testing
  - Concurrent orders (race condition test)
  - Stock depletion scenarios
  - Invalid state transitions
  - Token expiration & refresh
- [ ] **12.5** Load testing (optional but recommended)
  - 100 concurrent requests test
  - Database connection pool sizing

**Tuần 12b (2.5 ngày) — Hardening & Docs**
- [ ] **12.6** Security hardening (fix audit issues)
  - [ ] DB password from environment: ✅ Week 3
  - [ ] JWT secret from environment: ✅ Week 3
  - [ ] Cookie Secure=true in HTTPS: Add to SecurityConfig conditional
  - [ ] Add X-CSRF-Token header if CSRF re-enabled
  - [ ] Add security headers (X-Content-Type-Options, X-Frame-Options, etc.)
  - [ ] Remove sensitive data from logs (use sanitizer)
  - [ ] Add rate limiting middleware (optional but good)
- [ ] **12.7** API documentation
  - Generate Swagger JSON from annotations
  - Test all Swagger examples
  - Export Postman collection from Swagger
  - Create README with API usage examples
- [ ] **12.8** Deploy-ready checklist
  - [ ] All tests pass
  - [ ] Code coverage > 60%
  - [ ] No WARN/ERROR in logs
  - [ ] Swagger UI accessible
  - [ ] Environment variables documented (.env.example)
  - [ ] Database migration script ready (or Flyway)
  - [ ] Docker setup (optional, for containerization)
- [ ] **12.9** Documentation finalization
  - Update README.md with setup instructions
  - Update docs/CONTROLLER_FLOW.md with actual implementation
  - Create DEPLOYMENT.md (environment setup, server config)
  - Create TESTING.md (test execution, coverage report)

**Tuần 12c (1.5 ngày) — Final Review & Deployment**
- [ ] **12.10** Code review
  - All PRs reviewed, merged to main
  - No hardcoded secrets
  - Consistent naming conventions
  - No dead code
- [ ] **12.11** Final testing on staging
  - Deploy to staging server
  - Smoke tests on all endpoints
  - Database backup testing
  - Rollback plan documented
- [ ] **12.12** Deployment to production (if applicable)
  - [ ] Backup current database
  - [ ] Run migration scripts
  - [ ] Start new backend
  - [ ] Monitor logs for errors
  - [ ] Verify all endpoints responsive
  - [ ] Alert on failures

**Deliverable Week 12:** Production-ready backend, fully tested & documented

**Estimation:** 3-4 developers × 1 week, 3-4 person-weeks

---

## 📊 RESOURCE ALLOCATION (12 Weeks)

### Team Composition (Estimated)
- **Backend Lead** (1): oversee architecture, security, complex logic (Orders, Bookings)
- **Senior Backend Dev** (2): Entity/service implementation, transactions
- **Mid-level Backend Dev** (2): Controllers, DTOs, endpoints
- **QA Engineer** (1): testing, security checks, documentation

**Total: 6 people × 12 weeks = 72 person-weeks of effort**

### Weekly workload per person
- Week 1-2: 4 devs at 100% (Database + Infrastructure)
- Week 3-4: 2-3 devs at 100% (Security)
- Week 5-6: 2 devs at 100% (Product management)
- Week 7-8: 3 devs at 100% (Orders — most complex)
- Week 9-10: 3 devs at 100% (Bookings — core business)
- Week 11: 1-2 devs at 100% (PayPal)
- Week 12: 3-4 devs at 100% (Testing, hardening, deployment) + QA

---

## ⚠️ RISKS & MITIGATION

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Transaction bugs (inventory corruption) | HIGH | CRITICAL | Start Transaction testing Week 7, not Week 12 |
| JWT/Security misconfig | HIGH | CRITICAL | Reference `04_SECURITY.md` exactly, no shortcuts |
| Data-level auth bypass (SALE sees all customers) | MEDIUM | HIGH | Implement filtering in Service layer, not Controller; test Week 10 |
| PayPal integration delays | MEDIUM | MEDIUM | Use sandbox mode, mock responses in tests Week 11 |
| Scope creep (WebSocket, file upload, etc.) | MEDIUM | HIGH | Stick to spec, prioritize core features |

---

## ✅ SIGN-OFF CRITERIA (Project Completion)

- [ ] All 11 entities fully mapped + tested
- [ ] JWT authentication working (login, refresh, logout)
- [ ] 4 roles with correct permissions (Admin, Management, Sale, Technician)
- [ ] Products & Orders CRUD complete
- [ ] **Maintenance booking 3-step flow** fully functional + tested
- [ ] Transactions atomic (no partial stock updates)
- [ ] PayPal sandbox integration complete
- [ ] Security audit findings resolved
- [ ] All endpoints documented in Swagger
- [ ] >= 60% unit test coverage
- [ ] Zero critical/high OWASP vulnerabilities
- [ ] Deployable to production (environment config externalized)
- [ ] README + deployment guides written

---

## 📚 KEY DOCUMENTS REFERENCE

| File | When to read |
|---|---|
| `00_PROJECT_CONTEXT.md` | Week 1 (overview) |
| `01_DATABASE_SCHEMA.md` | Week 1-2 (entities) |
| `02_BUSINESS_LOGIC.md` | Week 7-10 (order/booking flows) |
| `03_API_SPECS.md` | Week 5-12 (all endpoints) |
| `04_SECURITY.md` | Week 3 (JWT/auth) + Week 12 (hardening) |
| `05_CODING_STEPS.md` | Each week (code structure) |
| `06_PAYPAL.md` | Week 11 (payment) |
| `CONTROLLER_FLOW.md` | Week 5+ (architecture reference) |
| `security_audit_report.md` | Week 3 + 12 (vulnerabilities to fix) |

---

## 🎯 SUCCESS METRICS

- **Code Quality:** <= 5 code smells per file, >= 60% test coverage
- **Performance:** API response time < 500ms for 95th percentile
- **Security:** 0 critical, 0 high vulnerabilities in OWASP audit
- **Reliability:** 99% uptime test (simulated 1000 requests)
- **Documentation:** Every public method has Javadoc + Swagger annotation

---

**Plan Version:** 1.0  
**Last Updated:** May 2026  
**Status:** Ready for implementation
