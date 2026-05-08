# ANGIA BACKEND - QUICK REFERENCE CHECKLIST & KANBAN

**Print this for weekly standups & progress tracking**

---

## 🎯 PROJECT MILESTONES (GO/NO-GO GATES)

```
✅ GATE 1 (End Week 2): Database + Entities Ready
   □ MySQL schema 11 tables created
   □ All JPA entities compile without error
   □ All repositories generate Spring Data code
   □ Connection test passes
   
✅ GATE 2 (End Week 4): Authentication Ready
   □ JWT login returns access token
   □ Refresh token in HttpOnly cookie works
   □ Logout clears cookie & token
   □ Disabled account → 403
   
✅ GATE 3 (End Week 6): Product Management Ready
   □ GET /products with filters works
   □ POST /products + images working
   □ Soft delete tested
   
✅ GATE 4 (End Week 8): ORDER STATE MACHINE LOCKED ⭐
   □ PENDING → PROCESSING → COMPLETED → CANCELLED transitions validated
   □ Stock decrements on COMPLETED (atomically)
   □ Stock increments on CANCELLED (from COMPLETED)
   □ Insufficient stock throws exception + rolls back
   □ Concurrent orders tested
   
✅ GATE 5 (End Week 10): BOOKING SYSTEM LIVE ⭐
   □ PENDING → CONFIRMED → COMPLETED flow works
   □ Technician assignment working
   □ Only assigned tech can complete
   □ Tech sees own bookings only (data-level auth)
   □ All state transitions locked
   
✅ GATE 6 (End Week 11): PayPal Sandbox Ready
   □ Create payment returns PayPal URL
   □ Execute payment updates order status
   □ Currency conversion working (1M VND ~ $40)
   
✅ GATE 7 (End Week 12): READY FOR PRODUCTION
   □ 0 critical vulnerabilities
   □ >= 60% test coverage
   □ All environment variables externalized
   □ Swagger UI accessible
   □ Deployment tested on staging
```

---

## 📅 WEEKLY STATUS TEMPLATE

**Week: ___  Status: [ ] ON TRACK  [ ] DELAYED  [ ] AT RISK**

### Completed (✅)
- [ ] Task 1
- [ ] Task 2

### In Progress (🔄)
- [ ] Task A (Est. complete: Day __)
- [ ] Task B (Est. complete: Day __)

### Blocked / At Risk (⚠️)
- [ ] Task X — Reason: ___________
  Mitigation: ___________
  Owner: ___

### Metrics
| Metric | Target | Actual | Status |
|---|---|---|---|
| Compilation | 100% | __% | [ ] ✅ [ ] ❌ |
| Unit tests | 70%+ | __% | [ ] ✅ [ ] ❌ |
| Code review | 100% | __% | [ ] ✅ [ ] ❌ |
| Security | 0 critical | ___ | [ ] ✅ [ ] ❌ |

### Risks & Next Week
- Risk 1: ___________
- Risk 2: ___________
- Next week priority: ___________

---

## 🧠 CRITICAL REMEMBER (Copy to desk!)

| Item | MUST NOT | MUST DO |
|---|---|---|
| **JWT** | Use `jjwt` or `io.jsonwebtoken` | Use `NimbusJwtDecoder` + Spring OAuth2 Resource Server |
| **Secrets** | Hardcode in properties | Use environment variables + .env.example |
| **Entity** | Expose directly to Controller | Always use DTOs |
| **Transactions** | Forget `@Transactional(rollbackFor=Exception.class)` | Add to ALL write methods in Service |
| **Passwords** | Plain text | Hash with Spring Security |
| **Roles** | Forget data-level filtering | Filter in Service, not Controller |
| **Logs** | Log tokens, PII, stack traces in prod | Log only business events + errors |
| **Tests** | Skip authorization tests | Test every role × endpoint combo |

---

## 📋 ENTITY DEPENDENCY CHECKLIST

```
✅ Create entities in this order (due to FK constraints):

Week 1-2:
  [ ] Role (no deps)
  [ ] User (→ Role)
  [ ] Customer (→ User[created_by])
  [ ] Brand (no deps)
  [ ] Product (→ Brand)
  [ ] ProductImage (→ Product)
  [ ] Order (→ Customer, User[sale_id])
  [ ] OrderItem (→ Order, Product)
  [ ] Service
  [ ] MaintenanceBooking (→ Customer, Service, User[technician_id])
  [ ] RefreshToken (→ User)
```

---

## 🔐 SECURITY AUDIT FIXES CHECKLIST

**From security_audit_report.md — MUST FIX by Week 12:**

- [ ] **CRITICAL:** Remove hardcoded DB password (Week 3)
- [ ] **CRITICAL:** Remove hardcoded JWT secret (Week 3)
- [ ] **CRITICAL:** Set Cookie `secure=true` for HTTPS (Week 3)
- [ ] **HIGH:** Add null checks in JWT extraction (All weeks)
- [ ] **HIGH:** Enforce strong password policy (Week 4)
- [ ] **HIGH:** Disable WebSocket anonymous access (Week 3)
- [ ] **HIGH:** Remove sensitive data from logs (Week 12)
- [ ] **HIGH:** Add security headers (X-Content-Type, X-Frame-Options) (Week 12)
- [ ] **MEDIUM:** Add rate limiting (Week 12)
- [ ] **MEDIUM:** Update PayPal SDK (Week 11)

**Verification:** Run `security_audit_report.md` checklist in Week 12 → should be 100% fixed.

---

## 🧪 TRANSACTION TESTING CHECKLIST

**CRITICAL: These tests MUST pass or release is blocked!**

### Inventory Atomicity Tests (Week 8)

```java
// TEST 1: Normal flow
Order(3 items) → COMPLETED → all stock decreases → ✅

// TEST 2: Out of stock
Order(5 items, item#3 out of stock) → COMPLETED → all 5 items NO change → ✅

// TEST 3: Concurrent orders (race condition)
Thread 1: Order(10 qty), Stock=12
Thread 2: Order(10 qty), Stock=12
Result: Either both succeed (stock=-8) OR only 1 succeeds → ✅

// TEST 4: Rollback on exception
Order COMPLETED, then CANCELLED → stock restored → ✅

// TEST 5: Multiple orders interdependency
Order 1(5 items)→COMPLETED: stock=5
Order 2(10 items)→COMPLETED: OUT OF STOCK, rolls back → stock=5 (unchanged)
→ ✅
```

**How to test:**
```bash
# Run integration test
mvn test -DuserProperty=integration -Dtest=OrderServiceTransactionTest

# Check logs for @Transactional checkpoint messages
# Verify database state matches expectations
```

---

## 🛡️ AUTHORIZATION MATRIX (For testing Week 10+)

**Every row = tested or release blocked!**

| Endpoint | ADMIN | MANAGEMENT | SALE | TECHNICIAN | Public |
|---|---|---|---|---|---|
| POST /auth/login | ✅ | ✅ | ✅ | ✅ | ✅ |
| GET /products | ✅ | ✅ | ✅ | ✅ | ✅ |
| POST /products | ✅ | ✅ | ❌ | ❌ | ❌ |
| POST /orders | ✅ | ❌ | ✅ | ❌ | ❌ |
| GET /orders | ✅ | ✅ | ✅* | ❌ | ❌ |
| PATCH /orders/{id}/status | ✅ | ✅ | ❌ | ❌ | ❌ |
| GET /maintenance-bookings | ✅ | ✅ | ✅ | ✅* | ❌ |
| POST /maintenance-bookings/{id}/assign | ✅ | ✅ | ❌ | ❌ | ❌ |
| PATCH /maintenance-bookings/{id}/complete | ✅ | ❌ | ❌ | ✅* | ❌ |

**Legend:**
- ✅ = Allowed all access
- ❌ = Forbidden (403)
- ✅* = Data-level filter (see own only)

**Test with curl:**
```bash
# Test SALE trying to access another SALE's orders
curl -H "Authorization: Bearer <SALE_B_TOKEN>" \
     http://localhost:8080/api/v1/orders?sale_id=<SALE_A_ID>
# Expected: 403 or filtered list of own orders only
```

---

## 📊 DEFINITION OF DONE (For each story/task)

- [ ] Code written + compiles
- [ ] Unit tests written (for Service/business logic)
- [ ] Integration tests pass (for endpoints)
- [ ] Code reviewed + approved by lead
- [ ] Swagger/OpenAPI annotations added
- [ ] Security checked (no hardcoded values, no sensitive logs)
- [ ] Performance verified (response time < 500ms)
- [ ] Database query verified (no N+1)
- [ ] Documentation updated (README/CONTROLLER_FLOW.md)
- [ ] Merged to main branch

---

## 🚨 IF STUCK / BLOCKED

**Is X task blocked?** Use this decision tree:

```
Q: Is it an Entity/Repository issue?
├─Y → Contact SBD1 (Entity layer lead)
└─N → Q: Is it a Security/JWT issue?
      ├─Y → Contact SBD2 (Security lead)
      └─N → Q: Is it a @Transactional / State machine issue?
            ├─Y → Contact SBD1 (Transaction expert)
            └─N → Q: Is it a Controller/routing issue?
                  ├─Y → Contact MBD2 (Controller lead)
                  └─N → Contact BL (escalate to lead) ← should be rare!
```

**Template for asking for help:**
```
Title: [BLOCKED] {Component} - {Issue}
Blocker: I'm stuck on [specific line/method]
Error: {exact error message}
Tried: {what I've already tried}
Impact: This blocks {what downstream}
ETA needed by: {when}

Context: {link to code / screenshot}
```

---

## 📞 DAILY STANDUP (15 MIN FORMAT)

**Each person answers:**
1. **Yesterday:** {What I completed}
2. **Today:** {What I'm working on}
3. **Blocker:** {If blocked: who can help?}
4. **Metric:** {Tests green? Code compiles? 0 warnings?}

**Example:**
```
SBD1: 
- Yesterday: Completed MaintenanceBookingService.assignTechnician()
- Today: Working on completeBooking() validation
- Blocker: None
- Metric: 12 tests pass, 1 integration test pending review

QA:
- Yesterday: Tested order transaction scenarios, found edge case
- Today: Writing concurrent order test
- Blocker: SBD1 - need to review transaction logic before PR merge
- Metric: 89 tests pass, test coverage 58%
```

---

## 🎯 END-TO-END SMOKE TEST (Week 12)

**Must pass 100% before production:**

```bash
#!/bin/bash

API="http://localhost:8080/api/v1"

# 1. Login
TOKEN=$(curl -X POST $API/auth/login \
  -d '{"username":"admin","password":"pass"}' \
  -H "Content-Type: application/json" | jq -r '.data.accessToken')

echo "✅ Login: $TOKEN"

# 2. Get products
curl -X GET "$API/products?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

echo "✅ Get products"

# 3. Create customer
curl -X POST $API/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test","phone":"0901234567","address":"addr"}'

echo "✅ Create customer"

# 4. Create order (then check transaction!)
# ... (abbreviated)

# 5. Create booking
# ... (abbreviated)

# 6. Verify Swagger
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/swagger-ui.html

echo "✅ ALL TESTS PASS - Ready to deploy!"
```

---

## 📈 PROJECT HEALTH DASHBOARD

**Update weekly (assign to QA):**

```
Week: __

GATES PASSED:
  □ Gate 1 ✅  □ Gate 2 ✅  □ Gate 3 ✅  □ Gate 4 ⏳  □ Gate 5 ⏳  □ Gate 6 ⏳  □ Gate 7 ⏳

CODE QUALITY:
  Compilation:     ✅ 100%
  Tests passing:   __ / __ (__)%
  Test coverage:   __% (target: 60%+)
  Code review:     ✅ All PRs reviewed
  Security audit:  __ critical / __ high (target: 0/0)

SCHEDULE:
  On track:  ✅ / ❌
  Days behind: __
  Critical path: {current blocking task}

TEAM:
  Velocity: __ story points
  Capacity: ✅ all assigned
  Blockers: {list any}

RISKS:
  1. {risk} - likelihood: ⏳ medium - mitigation: {plan}
  2. {risk} - likelihood: 🔴 high - mitigation: {plan}
```

---

## 🎓 REFERENCE DOCUMENTS (Pin to slack)

1. **00_PROJECT_CONTEXT.md** - Read Week 1
2. **01_DATABASE_SCHEMA.md** - Read Week 1-2
3. **02_BUSINESS_LOGIC.md** - Read Week 7-10
4. **03_API_SPECS.md** - Read Week 5+
5. **04_SECURITY.md** - Read Week 3 + 12
6. **05_CODING_STEPS.md** - Read each step
7. **06_PAYPAL.md** - Read Week 11
8. **CONTROLLER_FLOW.md** - Read Week 5+
9. **12WEEK_PROJECT_PLAN.md** - Read Week 1 (this file)
10. **DETAILED_TASK_ASSIGNMENT.md** - Read Week 1 (task allocation)
11. **security_audit_report.md** - Fix all issues by Week 12

---

**Version:** 1.0  
**Updated:** May 2026  
**Print & Post in Team Room!**
