# ANGIA BACKEND - 12 WEEK VISUAL ROADMAP

**This is a high-level view of what happens each week and the parallel work streams.**

---

## 📊 GANTT CHART - WORK STREAMS

```
LEGEND:
█████ = High parallel work (4+ devs)
████  = Medium parallel work (2-3 devs)
███   = Low parallel work (1-2 devs)
▓▓▓▓  = Dependency/Waiting period
━━━  = Critical path (blocks downstream)
⭐   = CORE BUSINESS LOGIC (highest priority quality)

───────────────────────────────────────────────────────────────────
WEEK 1-2: INFRASTRUCTURE
───────────────────────────────────────────────────────────────────
SBD1  [█████████ Entity Layer + Repositories]
SBD2  [█████████ Config + Exception Handling]
MBD1  [████████ ApiResponse + Enums + Custom Exceptions]
MBD2  [████████ Repository queries + DDL]
QA    [███░░░░░░ Database setup + test data]
      └─━━━━━━━━━┛ GATE 1 CHECK


───────────────────────────────────────────────────────────────────
WEEK 3-4: AUTHENTICATION & SECURITY (⚠️ CRITICAL PATH)
───────────────────────────────────────────────────────────────────
SBD2  [█████ JWT Service] [█████ UserDetailsService]
      [█████ SecurityConfig] [███ Cookie handling]
      └─━━━━━━━━━┛ GATE 2 CHECK
MBD2  [████ AuthController] [████ Auth DTOs]
QA    [███░░░░░░ Auth tests + flow validation]
      (SBD1 waiting on JWT service completion)
      (PayPal blocked until Week 11)


───────────────────────────────────────────────────────────────────
WEEK 5-6: PRODUCT MANAGEMENT
───────────────────────────────────────────────────────────────────
MBD1  [█████ BrandService] [█████ ProductService]
      [█████ BrandController] [█████ ProductController]
MBD2  [███░░░ Can start image upload in 5b]
      └─━━━━━━━━━┛ GATE 3 CHECK
QA    [████ CRUD tests] [████ Filtering tests] [████ Pagination tests]
      (SBD1 + SBD2 can prep for Orders in background)


───────────────────────────────────────────────────────────────────
WEEK 7-8: ORDER MANAGEMENT ⭐ (HIGHEST QUALITY RISK)
───────────────────────────────────────────────────────────────────
SBD1  [█████████ CustomerService] 
      [██████████ OrderService.create() validation]
      [██████████ TRANSACTION LOGIC @Transactional]
      [████ OrderService.updateStatus() state machine]
      └─━━━━━━━━━┛ GATE 4 CHECK ← CANNOT SKIP
MBD2  [████ OrderController] [████ DTOs]
MBD1  [██ Data-level auth helpers] [██ DTOs prep]
QA    [██████████ Transaction tests (critical!)] 
      [██████████ State machine edge cases]
      [████ Data-level auth verification tests]
      ⚠️  IF Gate 4 FAILS: WEEK 8 REPEAT (cannot proceed to bookings)
      
      
───────────────────────────────────────────────────────────────────
WEEK 9-10: MAINTENANCE BOOKING ⭐ (CORE BUSINESS — Parallel Week 10)
───────────────────────────────────────────────────────────────────
SBD1  [██████████ MaintenanceBookingService (create, assign, complete)]
      [█████ State machine validation (PENDING→CONFIRMED→COMPLETED)]
      [████ Data-level auth (Tech sees own bookings)]
      └─━━━━━━━━━┛ GATE 5 CHECK ← CANNOT SKIP
MBD2  [████░░░ Can start MaintenanceBookingController in 10a]
      [██████ MaintenanceBookingController (7 endpoints + Swagger)]
MBD1  [██ Booking DTOs (parallel with SBD1)]
QA    [████░░░░░ Prep authorization test matrix in 9b]
      [██████████ Authorization tests (role × endpoint)]
      [████████ State machine transition tests]
      [██████ E2E booking flow tests]


───────────────────────────────────────────────────────────────────
WEEK 11: PAYPAL INTEGRATION (Can parallelize with WEEK 10 booking testing)
───────────────────────────────────────────────────────────────────
SBD2  [███ PaypalConfig] [███ Env setup]
MBD2  [████ PaypalService] [████ PaymentController]
      [████ Currency conversion (VND→USD)]
MBD1  [██ PaymentRequest/Response DTOs]
QA    [███░░░░░░ Sandbox test setup]
      [████ Payment flow tests]
      └─━━━━━━━━━┛ GATE 6 CHECK


───────────────────────────────────────────────────────────────────
WEEK 12: FINAL TESTING, HARDENING & DEPLOYMENT ⚠️ CONVERGENCE
───────────────────────────────────────────────────────────────────
ALL   [██████████ Unit test coverage push (target 60%+)]
      [██████████ Integration test completion]
      [██████████ Authorization matrix verification]
QA    [██████████ Smoke tests] [████ Staging deployment]
SBD2  [██████ Security hardening (6 audit fixes)]
      [██████ Environment externalization]
BL    [████ Code review + security audit]
      [████ Deployment checklist]
ALL   [████ Final sign-off & lessons learned]
      └─━━━━━━━━━┛ GATE 7 CHECK ← PRODUCTION READY


───────────────────────────────────────────────────────────────────
```

---

## 📈 DEPENDENCY GRAPH

```
┌─────────────────────────────────────────────────────────────┐
│ WEEK 1-2: Database + Entities                               │
│ (NO DEPENDENCIES — Start here!)                             │
└───────────────────┬─────────────────────────────────────────┘
                    │
        ┌───────────┴───────────┐
        ▼                       ▼
    ┌─────────────┐    ┌──────────────┐
    │WEEK 3-4     │    │WEEK 5-6      │
    │Auth & JWT   │    │Products      │
    │(GATE 2)     │    │(GATE 3)      │
    └─────┬───────┘    └────┬─────────┘
          │                 │
          │  ┌──────────────┘
          │  │  
          ▼  ▼
    ┌────────────────┐
    │WEEK 7-8        │  ⭐ TRANSACTION-CRITICAL
    │Orders          │
    │(GATE 4)        │
    └────────┬───────┘
             │
          ┌──┴──┐
          ▼     │
    ┌─────────────────────┐
    │WEEK 9-10            │  ⭐ CORE BUSINESS
    │Bookings             │
    │(GATE 5)             │
    └──────┬──────────────┘
           │
      ┌────┴────┐
      ▼         ▼ (parallel)
    WEEK 11   WEEK 10-TEST
    PayPal    (continue booking tests)
    (GATE 6)
          ┌────┘
          ▼
    ┌──────────────┐
    │WEEK 12       │
    │Testing +     │
    │Hardening +   │
    │Deployment    │
    │(GATE 7)      │
    └──────────────┘
         ▼
    READY FOR PRODUCTION ✅
```

---

## 👥 TEAM CAPACITY ALLOCATION BY WEEK

```
          |Week1|Week2|Week3|Week4|Week5|Week6|Week7|Week8|Week9|Week10|Week11|Week12|
─────────┼─────┼─────┼─────┼─────┼─────┼─────┼─────┼─────┼─────┼──────┼──────┼──────┤
SBD1      | 100%| 80% | 20% | 10% | 20% | 20% | 100%| 100%| 100%| 100% | 30%  | 40%  |
SBD2      | 100%| 100%| 100%| 100%| 10% | 10% | 20% | 20% | 10% | 10%  | 60%  | 50%  |
MBD1      | 80% | 80% | 20% | 20% | 100%| 100%| 50% | 50% | 30% | 30%  | 30%  | 40%  |
MBD2      | 80% | 80% | 10% | 50% | 50% | 50% | 70% | 70% | 40% | 60%  | 40%  | 40%  |
BL        | 60% | 40% | 30% | 20% | 20% | 20% | 30% | 40% | 20% | 30%  | 20%  | 80%  |
QA        | 60% | 80% | 50% | 60% | 70% | 80% | 80% | 90% | 80% | 90%  | 70%  | 100% |
─────────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴──────┴──────┴──────┘
  Legend:  100% = Full week commitment
           80%  = Mostly assigned (some flex for other tasks)
           30%  = Partial assignment
           10%  = Light touch / standby
```

---

## 🎯 CRITICAL PATH vs. SLACK PATH

```
┌─────────────────────────────────────────────────────────────┐
│ CRITICAL PATH (if delayed, entire project delayed)          │
│ DO NOT parallelize or skip these weeks:                      │
├─────────────────────────────────────────────────────────────┤
│ ✓ Week 1-2   Database (0 days slack)                       │
│ ✓ Week 3-4   Authentication (2 days slack max)              │
│ ✓ Week 7-8   Orders (MUST fix by Friday or repeat week)     │
│ ✓ Week 9-10  Bookings (MUST work before moving to 11)       │
│ ✓ Week 12    Testing + Hardening (0 days slack)             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ SLACK PATH (can shift ~2 weeks without impacting end date)  │
├─────────────────────────────────────────────────────────────┤
│ ~ Week 5-6   Products (Could move to Week 5.5 if needed)    │
│ ~ Week 11    PayPal (Can delay if started in final 2 weeks) │
│              (But NOT recommended — keep schedule)           │
└─────────────────────────────────────────────────────────────┘

⚠️ BUFFERING RULE:
   Most critical items (Week 7-8, 9-10) have NO BUFFER.
   If transaction tests fail Week 8, must repeat entire Week 8!
```

---

## 📅 MILESTONE TIMELINE

```
PROJECT START
    │
    ├─ Week 2 Friday: GATE 1 (Database ready)
    │
    ├─ Week 4 Friday: GATE 2 (Auth login works)
    │
    ├─ Week 6 Friday: GATE 3 (Products CRUD works)
    │
    ├─ Week 8 Friday: GATE 4 ⭐ (Transaction tests MUST pass)
    │                  ⚠️ No exceptions! If fail → repeat week!
    │
    ├─ Week 10 Friday: GATE 5 ⭐ (Booking state machine works)
    │                   ⚠️ CORE BUSINESS — High quality required!
    │
    ├─ Week 11 Friday: GATE 6 (PayPal sandbox working)
    │
    ├─ Week 12 Friday: GATE 7 ✅ PRODUCTION READY
    │
PROJECT COMPLETION
    │
    └─ Week 12 Friday: Deployment to production (if applicable)
```

---

## 🚨 WHAT COULD GO WRONG (Risk Timeline)

```
WEEK 1-2:
  Risk: Database schema complexity
  Impact: ★★☆☆☆ (Medium)
  Recovery: Re-review schema with BL
  Mitigation: Get architect approval before writing DDL

WEEK 3-4:
  Risk: JWT/Spring Security misconfiguration
  Impact: ★★★★☆ (High — blocks auth)
  Recovery: 2-3 days to debug + fix
  Mitigation: Follow 04_SECURITY.md exactly, no shortcuts

WEEK 7-8: ⚠️ HIGHEST RISK ZONE
  Risk: Transaction atomicity bugs
  Impact: ★★★★★ (CRITICAL — data corruption potential)
  Recovery: If tests fail Friday → entire week repeats
  Mitigation: Start transaction testing early (Monday), not Friday

WEEK 9-10:
  Risk: Data-level auth bypass (SALE sees all customers)
  Impact: ★★★★☆ (High — security issue)
  Recovery: 1-2 days per auth issue found
  Mitigation: Test auth matrix thoroughly in Week 10

WEEK 11:
  Risk: PayPal API integration complexity
  Impact: ★★★☆☆ (Medium — non-blocking)
  Recovery: 2-3 days
  Mitigation: Use sandbox heavily, mock responses in tests

WEEK 12:
  Risk: Hidden bugs in integration testing
  Impact: ★★★★★ (CRITICAL — can't release with bugs)
  Recovery: 3-5 days of bug fixes
  Mitigation: Start integration tests in Week 10, not Week 12

OVERALL:
  Probability of on-time delivery: ~75–80% (with team of 6)
  Contingency budget: +2 weeks recommended (total 14 weeks)
```

---

## 📊 BURN-DOWN CHART (Expected Progress)

```
Remaining Work
      │
 100% │  ╲
      │   ╲
  80% │    ╲___
      │        ╲
  60% │         ╲___
      │             ╲
  40% │              ╲___
      │                  ╲
  20% │                   ╲___
      │                       ╲
   0% │                        └─ ✅
      └────────────────────────────────────
        Week1  Week2  Week3  Week4  ...  Week12

      Ideal Line (constant burn)
      Actual Line (usually has bumps)

Expected bumps:
  - Week 8: Auth tests discovered edge cases (small bump)
  - Week 10: Booking tests reveal data-level auth issues (medium bump)
  - Week 12: Integration test failures (large bump if not prepared)
```

---

## ✅ WEEKLY SPRINT SUMMARY TEMPLATE

**Copy for each Monday's standup:**

```
SPRINT WEEK {N}/12 — {DATES}
Mission: {Component being built}
Capacity: {# devs at 100%} + {# devs at 50%, etc.}

┌─ GATE CHECK ─────────────────────────────────────────┐
│ Previous GATE: [ ] ✅ PASSED   [ ] ⚠️  AT RISK
│ This week's GATE: {GATE N}
│ Go/No-Go criteria: {specific checklist}
└───────────────────────────────────────────────────────┘

WORK BREAKDOWN
- Task A: {Owner}      Est: {hours}  Priority: 🔴 Critical
- Task B: {Owner}      Est: {hours}  Priority: 🟠 High
- Task C: {Owner}      Est: {hours}  Priority: 🟡 Medium

RISKS IDENTIFIED
- Risk 1: {description} - Mitigation: {plan}
- Risk 2: {description} - Mitigation: {plan}

SUCCESS CRITERIA
[ ] All tasks completed by Friday EOD
[ ] Code compiles without warning
[ ] Tests pass (>70% for this week's component)
[ ] Code reviewed and merged
[ ] GATE criteria met

TEAM NOTES
DON'T FORGET:
- Deploy to local MySQL to test
- Run `mvn clean package` daily
- Update progress docs
- Sync with dependent team members
```

---

## 🎯 FINAL DELIVERY CHECKLIST

**By END of Week 12, verify:**

```
┌─ CODE QUALITY ────────────────────┐
[ ] Compilation: 100% success
[ ] Warnings: < 5 total
[ ] Code smells: < 2 per file
[ ] No hardcoded values/secrets
[ ] No debug code / System.out.println

┌─ TESTS ────────────────────────────┐
[ ] Unit test coverage: >= 60%
[ ] All tests pass locally
[ ] Integration tests: 100% pass
[ ] Staging deployment: successful
[ ] Smoke tests: all green

┌─ SECURITY ────────────────────────┐
[ ] 0 critical vulnerabilities
[ ] 0 high vulnerabilities
[ ] All audit issues fixed
[ ] Environment vars externalized
[ ] Secrets not in logs

┌─ DEPLOYMENT ────────────────────┐
[ ] .env.example created
[ ] Database migration ready
[ ] Docker (optional) working
[ ] Monitoring/logging configured
[ ] Runbook documented

┌─ DOCUMENTATION ─────────────────┐
[ ] README complete
[ ] API Swagger UI accessible
[ ] Postman collection exported
[ ] DEPLOYMENT.md written
[ ] TESTING.md written

┌─ SIGN-OFF ──────────────────────┐
[ ] Backend Lead approval: ✅ {Name}
[ ] QA approval: ✅ {Name}
[ ] Security review: ✅ {Name}
[ ] Ready for production: ✅ YES

    RELEASE DATE: Week 12 Friday
```

---

**Version:** 1.0  
**Last Updated:** May 2026  
**Status:** Ready to share with team
