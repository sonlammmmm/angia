# PHÂN CÔNG CÔNG VIỆC CHI TIẾT - 12 TUẦN ANGIA BACKEND

**Các vai trò (Recommended team):**
1. **Backend Lead (BL)** — Architecture, complex logic, security decisions
2. **Senior Backend Dev #1 (SBD1)** — Entity layer, Services (Orders, Bookings)
3. **Senior Backend Dev #2 (SBD2)** — Security, Authentication, Infrastructure
4. **Mid-level Backend Dev #1 (MBD1)** — Controllers, DTOs, Product/Brand
5. **Mid-level Backend Dev #2 (MBD2)** — Controllers, DTOs, Customer/Order
6. **QA Engineer (QA)** — Testing, documentation, deployment

---

## TUẦN 1: FOUNDATION PHASE (Database & Infrastructure)

| Task | Component | Owner | Estimation |
|---|---|---|---|
| Architecture review & approval | All | BL | 2h |
| Update pom.xml with dependencies | Build | SBD2 | 3h |
| Create ApiResponse + Enums | common/ | MBD1 | 4h |
| Create custom exceptions | exception/ | MBD1 | 3h |
| Create GlobalExceptionHandler | exception/ | SBD2 | 5h |
| Create CorsConfig | config/ | SBD2 | 2h |
| Design + DDL for 11 tables | database/ | BL | 6h |
| Create all Entity classes (Role→RefreshToken) | entity/ | SBD1 | 12h |
| Create all Repository interfaces | repository/ | MBD2 | 4h |
| Create MySQL schema script | database/ | BL + QA | 3h |
| Test database connection | DevOps | QA | 2h |
| Setup .gitignore, Maven config | Build | SBD2 | 2h |
| Initial test build (mvn clean package) | Build | SBD2 | 1h |
| **WEEK 1 TOTAL** | | | **49h (~12 person-days)** |

**Deliverable:** Database ready, all entities compile, first API call possible

---

## TUẦN 2: INFRASTRUCTURE (Continued) + Exception Handling

| Task | Component | Owner | Estimation |
|---|---|---|---|
| Complete SecurityConfig skeleton | security/ | SBD2 | 4h |
| Test data setup (create test users in DB) | test/ | QA | 3h |
| Document architecture diagram | docs/ | BL | 3h |
| Entity integration testing (JPA tests) | test/ | MBD1 | 4h |
| Code review (Week 1 code) | Review | BL + SBD2 | 3h |
| Update application.properties | config/ | SBD2 | 2h |
| Performance baseline (DB query times) | DevOps | QA | 2h |
| Create project README v1 | docs/ | BL | 2h |
| **WEEK 2 TOTAL** | | | **23h (~6 person-days)** |

**Deliverable:** All infrastructure ready, tests passing, team ready for authentication

---

## TUẦN 3: SECURITY PHASE (Authentication) — PART 1

| Task | Component | Owner | Estimation |
|---|---|---|---|
| **SECURITY HARDENING** | | | |
| Fix: Move DB password to env vars | maintenance/ | SBD2 | 2h |
| Fix: Move JWT secret to env vars | maintenance/ | SBD2 | 2h |
| Fix: Create .env.example | docs/ | SBD2 | 1h |
| Create JwtService (token generation) | security/ | SBD2 | 6h |
| Create UserDetailsServiceImpl | security/ | SBD2 | 5h |
| Complete SecurityConfig (JwtDecoder, JwtEncoder) | security/ | SBD2 | 6h |
| Add auth exception handlers to GlobalExceptionHandler | exception/ | SBD2 | 3h |
| Create UserMapper (Entity ↔ UserDetails) | mapper/ | MBD1 | 2h |
| Test: JWT generation & validation | test/ | QA | 3h |
| Test: UserDetails loading | test/ | QA | 2h |
| **WEEK 3 TOTAL** | | | **32h (~8 person-days)** |

**Deliverable:** JWT infrastructure complete, security audit issues fixed

---

## TUẦN 4: SECURITY PHASE (Authentication) — PART 2

| Task | Component | Owner | Estimation |
|---|---|---|---|
| **AUTH SERVICE & CONTROLLER** | | | |
| Create AuthService (login, refresh, logout) | service/ | SBD2 | 8h |
| Create AuthController (3 endpoints) | controller/ | MBD2 | 5h |
| Create AuthRequest/Response DTOs | dto/ | MBD1 | 3h |
| Implement cookie handling (HttpOnly, Secure, SameSite) | controller/ | SBD2 | 3h |
| Test: Login flow end-to-end | test/ | QA | 4h |
| Test: Refresh token rotation | test/ | QA | 3h |
| Test: Logout invalidates token | test/ | QA | 2h |
| Test: Disabled account → 403 | test/ | QA | 2h |
| Create test documentation (curl examples) | docs/ | QA | 2h |
| Code review & integration test | review/ | BL | 3h |
| **WEEK 4 TOTAL** | | | **35h (~9 person-days)** |

**Deliverable:** Complete authentication working, all tests green

---

## TUẦN 5: PRODUCT MANAGEMENT — PART 1

| Task | Component | Owner | Estimation |
|---|---|---|---|
| Create Brand DTOs (Request, Response) | dto/ | MBD1 | 2h |
| Create BrandService (CRUD + soft delete) | service/ | MBD1 | 5h |
| Create BrandController (5 endpoints) | controller/ | MBD1 | 4h |
| Create Product DTOs | dto/ | MBD1 | 3h |
| Create ProductService (CRUD + filtering + soft delete) | service/ | MBD1 | 8h |
| Handle specs_json JSON serialization | service/ | MBD1 | 3h |
| Create ProductController (6 endpoints + query params) | controller/ | MBD2 | 6h |
| Test: Brand CRUD | test/ | QA | 3h |
| Test: Product filtering (type, brandId) | test/ | QA | 3h |
| Test: Pagination (page, size, sort) | test/ | QA | 2h |
| Swagger annotations on all endpoints | controller/ | MBD2 | 3h |
| **WEEK 5 TOTAL** | | | **42h (~11 person-days)** |

**Deliverable:** Brand & Product endpoints working with filtering

---

## TUẦN 6: PRODUCT MANAGEMENT — PART 2 + Image Upload

| Task | Component | Owner | Estimation |
|---|---|---|---|
| Create ProductImage entity & repository (if not done) | entity/ | SBD1 | 1h |
| Create ProductImageService (upload, set-main) | service/ | MBD1 | 5h |
| Create file storage implementation (uploads/images/) | config/ | MBD2 | 4h |
| Create ProductImage endpoints (POST, PATCH) | controller/ | MBD2 | 3h |
| Create ProductImageRequest/Response DTOs | dto/ | MBD1 | 2h |
| Test: Image upload functionality | test/ | QA | 4h |
| Test: Multiple images per product | test/ | QA | 2h |
| Test: Set-main image validation | test/ | QA | 2h |
| N+1 query optimization (EntityGraph testing) | performance/ | BL | 2h |
| Integration test (product → images whole flow) | test/ | QA | 3h |
| API documentation (Swagger + examples) | docs/ | QA | 2h |
| **WEEK 6 TOTAL** | | | **30h (~8 person-days)** |

**Deliverable:** Full product management with images, soft delete working

---

## TUẦN 7: CUSTOMER & ORDER MANAGEMENT — PART 1

| Task | Component | Owner | Estimation |
|---|---|---|---|
| Create CustomerService (CRUD + data-level auth for SALE) | service/ | MBD2 | 6h |
| Create CustomerController | controller/ | MBD2 | 4h |
| Create Customer DTOs | dto/ | MBD1 | 2h |
| Create getCurrentUserId() + getCurrentUserRole() helper | security/ | SBD2 | 2h |
| Create OrderService.create() (validate stock) | service/ | SBD1 | 8h |
| Create OrderItemService / repository queries | service/ | SBD1 | 3h |
| Create Order DTOs (Create, Update, Response) | dto/ | MBD1 | 3h |
| Create OrderController (basic CRUD first) | controller/ | MBD2 | 3h |
| Test: Customer creation & retrieval | test/ | QA | 2h |
| Test: Order creation with stock validation | test/ | QA | 3h |
| Test: Insufficient stock → error + no save | test/ | QA | 2h |
| **WEEK 7 TOTAL** | | | **38h (~10 person-days)** |

**Deliverable:** Customer CRUD, Order creation with stock validation

---

## TUẦN 8: ORDER MANAGEMENT — PART 2 (State Machine & Transactions) ⭐

| Task | Component | Owner | Estimation |
|---|---|---|---|
| **CRITICAL: OrderService.updateOrderStatus()** | | | |
| Implement state machine validation | service/ | SBD1 | 6h |
| Implement inventory update logic (decrement on COMPLETED) | service/ | SBD1 | 6h |
| Implement inventory restore logic (increment on CANCELLED) | service/ | SBD1 | 3h |
| Add @Transactional(rollbackFor=Exception.class) | service/ | SBD1 | 1h |
| Create OrderController.updateStatus() endpoint | controller/ | MBD2 | 2h |
| Create OrderStatusRequest DTO | dto/ | MBD1 | 1h |
| **TRANSACTION TESTING (CRITICAL)** | | | |
| Test: 3-item order → complete → all stock decreases | test/ | QA | 4h |
| Test: Item lacking stock → exception → no stock changes | test/ | QA | 4h |
| Test: 2 concurrent orders, 1 succeeds, 1 fails atomically | test/ | QA | 5h |
| Test: COMPLETED → CANCELLED → stock restored correctly | test/ | QA | 3h |
| Test: State machine (all invalid transitions rejected) | test/ | QA | 3h |
| **DATA-LEVEL AUTH** | | | |
| SALE user can only see own orders (sales_id filter) | service/ | MBD2 | 3h |
| Test: SALE doesn't see other users' orders | test/ | QA | 2h |
| Swagger documentation for all order endpoints | docs/ | QA | 2h |
| Code review (transaction logic is critical) | review/ | BL + SBD1 | 4h |
| **WEEK 8 TOTAL** | | | **49h (~13 person-days)** |

**Deliverable:** ✅ **ORDER STATE MACHINE COMPLETE & TRANSACTION-SAFE**

---

## TUẦN 9: MAINTENANCE BOOKING — PART 1 (Service Layer) ⭐

| Task | Component | Owner | Estimation |
|---|---|---|---|
| **CORE BOOKING SERVICE** | | | |
| Create ServiceService (CRUD for maintenance services) | service/ | MBD1 | 4h |
| Create MaintenanceBookingService.create() | service/ | SBD1 | 5h |
| Create MaintenanceBookingService.assignTechnician() | service/ | SBD1 | 5h |
| Create MaintenanceBookingService.completeBooking() (with tech verification) | service/ | SBD1 | 6h |
| Create MaintenanceBookingService.cancelBooking() | service/ | SBD1 | 3h |
| Create MaintenanceBookingService.getAll() with status/tech/date filters | service/ | SBD1 | 8h |
| Implement data-level auth (TECHNICIAN sees own bookings only) | service/ | SBD1 | 3h |
| Create Booking DTOs (Create, Assign, Complete, Response) | dto/ | MBD1 | 4h |
| Test: Create booking (PENDING) | test/ | QA | 2h |
| Test: Assign technician (PENDING → CONFIRMED) | test/ | QA | 2h |
| Test: Only assigned tech can complete | test/ | QA | 2h |
| Test: Technician filters to own bookings | test/ | QA | 2h |
| Test: State machine (all transitions) | test/ | QA | 2h |
| __WEEK 9 TOTAL__ | | | **48h (~12 person-days)** |

**Deliverable:** Booking service fully implemented + tested

---

## TUẦN 10: MAINTENANCE BOOKING — PART 2 (Controller & Integration) ⭐

| Task | Component | Owner | Estimation |
|---|---|---|---|
| Create MaintenanceBookingController (7 endpoints) | controller/ | MBD2 | 6h |
| Create ServiceController (CRUD) | controller/ | MBD2 | 3h |
| Add all Swagger @Operation + @ApiResponses annotations | controller/ | MBD2 | 4h |
| **AUTHORIZATION TESTING** | | | |
| Test: SALE creates booking → can see it | test scenario/ | QA | 2h |
| Test: TECHNICIAN → 403 on assign endpoint | test scenario/ | QA | 2h |
| Test: MANAGEMENT assigns → booking CONFIRMED | authorization/ | QA | 2h |
| Test: Wrong technician → 403 on complete | authorization/ | QA | 2h |
| Test: Assigned tech completes → 200 OK | authorization/ | QA | 2h |
| Test: All 4 roles on all endpoints (authorization matrix) | authorization/ | QA | 4h |
| **INTEGRATION TESTING** | | | |
| Integration: customer → bookings relationship | integration/ | QA | 2h |
| Integration: GET /customers/{id}/bookings endpoint | controller/ | MBD2 | 2h |
| Integration: service list includes all linked bookings | integration/ | QA | 2h |
| E2E: Create customer → order → booking flow | e2e/ | QA | 4h |
| **DATE RANGE FILTERING** | | | |
| Test: Query bookings for Aug 2025 | test feature/ | QA | 2h |
| Test: Pagination on booking list (50+ bookings) | test feature/ | QA | 2h |
| **DOCUMENTATION** | | | |
| Create Booking API doc with all examples | docs/ | QA | 3h |
| Create workflow diagram (Booking state machine visual) | docs/ | BL | 2h |
| **SECURITY REVIEW** | | | |
| Verify no hardcoded booking IDs in code | security review/ | BL | 1h |
| Code review all 7 booking endpoints | review/ | BL + SBD1 | 4h |
| **WEEK 10 TOTAL** | | | **47h (~12 person-days)** |

**Deliverable:** ✅ **FULL MAINTENANCE BOOKING SYSTEM LIVE** (core business complete)

---

## TUẦN 11: PAYPAL INTEGRATION

| Task | Component | Owner | Estimation |
|---|---|---|---|
| **PAYPAL SETUP** | | | |
| Create PaypalConfig.java (APIContext bean) | config/ | SBD2 | 3h |
| Add PayPal properties to application.properties | config/ | SBD2 | 2h |
| Create PaypalService.createPayment() | service/ | MBD2 | 6h |
| Create PaypalService.executePayment() | service/ | MBD2 | 5h |
| Handle PayPal exceptions & errors | service/ | MBD2 | 3h |
| **VND → USD CONVERSION** | | | |
| Integrate exchange rate API (OpenExchangeRates or similar) | service/ | MBD2 | 4h |
| Cache exchange rates (update daily) | service/ | MBD2 | 2h |
| Test: 1,000,000 VND conversion accuracy | test/ | QA | 2h |
| **PAYMENT CONTROLLER & DTO** | | | |
| Create PaymentController (2 endpoints: create, execute) | controller/ | MBD2 | 3h |
| Create PaymentCreateRequest/Response DTOs | dto/ | MBD1 | 2h |
| **INTEGRATION WITH ORDERS** | | | |
| Add paymentStatus, transactionId to Order entity | entity/ | SBD1 | 1h |
| Update OrderService: link payment status | service/ | SBD1 | 3h |
| Update OrderController: expose payment endpoints | controller/ | MBD2 | 1h |
| **TEST PAYPAL FLOW** | | | |
| Test: Create payment → verify PayPal URL generated | test/ | QA | 3h |
| Test: Execute payment → order status updated | test/ | QA | 3h |
| Test: Payment failure → order reverts to PENDING | test/ | QA | 2h |
| Test: Sandbox mode (mock responses) | test/ | QA | 2h |
| Test: Concurrent payments | test/ | QA | 2h |
| **DOCUMENTATION** | | | |
| Create PayPal flow diagram | docs/ | BL | 2h |
| Document sandbox credentials setup | docs/ | SBD2 | 1h |
| Swagger docs for payment endpoints | docs/ | QA | 2h |
| **WEEK 11 TOTAL** | | | **50h (~13 person-days)** |

**Deliverable:** PayPal sandbox integration working, order→payment flow complete

---

## TUẦN 12: TESTING, HARDENING & DEPLOYMENT

| Task | Component | Owner | Estimation |
|---|---|---|---|
| **UNIT TESTING** | | | |
| Service layer tests (70%+ coverage target) | test/ | QA + MBD1 | 12h |
| Exception handler tests | test/ | QA | 2h |
| JWT service tests | test/ | QA | 3h |
| **INTEGRATION TESTING** | | | |
| E2E: login → order → booking → payment | test/ | QA | 5h |
| E2E: All role scenarios (4 roles × major endpoints) | test/ | QA | 8h |
| Concurrent operations (race condition tests) | test/ | QA | 3h |
| Database transaction rollback tests | test/ | QA | 3h |
| **AUTHORIZATION MATRIX** | | | |
| Document & verify all 8 roles/endpoints combos | compliance/ | QA | 4h |
| **SECURITY HARDENING** | | | |
| Fix: Cookie Secure=true conditional (HTTPS detection) | security/ | SBD2 | 2h |
| Fix: Add security headers (X-Content-Type, X-Frame-Options, etc.) | config/ | SBD2 | 2h |
| Fix: Remove/sanitize sensitive logs | logging/ | SBD2 | 3h |
| Add rate limiting (optional but recommended) | security/ | SBD2 | 4h |
| Re-run security audit against code | audit/ | BL | 2h |
| **API DOCUMENTATION** | | | |
| Generate Swagger JSON + UI | docs/ | QA | 1h |
| Test all Swagger examples (curl execution) | docs/ | QA | 3h |
| Export Postman collection from Swagger | docs/ | QA | 1h |
| Create README with API usage examples | docs/ | BL + QA | 3h |
| Create DEPLOYMENT.md (env setup, server config) | docs/ | SBD2 + BL | 3h |
| Create TESTING.md (test execution, coverage report) | docs/ | QA | 2h |
| **CODE QUALITY** | | | |
| Code review: all components | review/ | BL | 5h |
| Check: no hardcoded secrets, IPs, paths | security-check/ | BL | 2h |
| Fix: any critical/high warnings in logs | maintenance/ | SBD1 + SBD2 | 3h |
| **DEPLOYMENT PREP** | | | |
| Create .env.example with all required variables | devops/ | SBD2 | 1h |
| Create database migration script (if using Flyway) | database/ | BL | 2h |
| Setup Docker (optional, containerization) | devops/ | SBD2 | 4h |
| Create deployment checklist | devops/ | BL + SBD2 | 2h |
| **STAGING TEST** | | | |
| Deploy to staging environment | devops/ | SBD2 + QA | 2h |
| Smoke tests on staging (all endpoints) | test/ | QA | 3h |
| Database backup & restore testing | devops/ | BL | 1h |
| Rollback plan documentation | devops/ | BL | 1h |
| **PRODUCTION DEPLOYMENT** (if applicable) | | | |
| Pre-deployment backup | devops/ | BL | 1h |
| Deploy to production | devops/ | SBD2 | 1h |
| Monitor logs post-deploy | monitoring/ | BL + QA | 2h |
| Verify all endpoints responsive | monitoring/ | QA | 1h |
| Alert setup on failures | devops/ | BL | 1h |
| **FINAL SIGN-OFF** | | | |
| Checklist: all tests green, 0 critical vulns | review/ | BL | 2h |
| Project retrospective & lessons learned | team/ | BL | 1h |
| **WEEK 12 TOTAL** | | | **92h (~23 person-days)** |

**Deliverable:** ✅ **PRODUCTION-READY BACKEND**

---

## 📊 SUMMARY BY PERSON (12 Weeks)

| Role | Total Hours | % of Total | Key responsibilities |
|---|---|---|---|
| **BL** (Backend Lead) | 85h | 14% | Architecture, critical decisions, code review, deployment |
| **SBD1** (Senior Backend Dev) | 120h | 20% | Entities, Service layer (Orders, Bookings), transactions |
| **SBD2** (Senior Backend Dev) | 110h | 18% | Security, JWT, config, hardening, deployment |
| **MBD1** (Mid Backend Dev) | 100h | 17% | DTOs, Product/Brand controllers, helper utilities |
| **MBD2** (Mid Backend Dev) | 95h | 16% | Customer/Order/Booking controllers, integration |
| **QA** (QA Engineer) | 90h | 15% | Testing, documentation, security verification |
| **TOTAL** | **600h** | 100% | ~72 person-days (12 weeks × 6 people) |

---

## 🚨 CRITICAL PATH

The following components are on the **critical path** (if delayed, entire project delays):

1. **Week 1-2:** Database schema + entities (blocks everything)
2. **Week 3-4:** JWT authentication (blocks authorization testing)
3. **Week 7-8:** Order state machine + transactions (blocks booking logic)
4. **Week 9-10:** Booking service + controller (core business)
5. **Week 12:** Security audit fixes + final testing

**Recommendation:** Assign **SBD1 + SBD2 + BL** to critical path items. Other items can be parallelized.

---

## 📋 DEPENDENCIES & BLOCKERS

```
Week 1-2:   [Database + Entities]
              ↓
Week 3-4:   [Authentication] ← blocks all subsequent auth testing
              ↓
Week 5-6:   [Product Management] ← needed for orders
              ↓
Week 7-8:   [Orders] ← needed for booking integration
              ↓
Week 9-10:  [Bookings] ← core business, can run parallel to PayPal
              ↓
Week 11:    [PayPal] ← independent, can parallelize with booking testing
              ↓
Week 12:    [Testing + Hardening + Deployment] ← depends on all above
```

---

## ✅ WEEKLY GO/NO-GO DECISION Points

| Week | Go/No-Go Criteria | Owner |
|---|---|---|
| End of Week 2 | DB connects, entities compile, no ORM errors | BL |
| End of Week 4 | JWT login/refresh working, 100% auth tests pass | SBD2 + QA |
| End of Week 6 | Product CRUD + images working, soft delete tested | MBD1 + QA |
| End of Week 8 | **Transaction tests MUST pass** (stock integrity) | SBD1 + QA |
| End of Week 10 | **Booking state machine MUST work** (core business) | SBD1 + QA |
| End of Week 11 | PayPal sandbox working in non-prod environment | MBD2 + QA |
| End of Week 12 | 0 critical vulns, >=60% test coverage, ready to deploy | BL + QA |

---

**Status:** Ready for assignment  
**Version:** 1.0  
**Last Updated:** May 2026
