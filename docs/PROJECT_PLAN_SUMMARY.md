# 📋 ANGIA BACKEND 12-WEEK PROJECT PLAN — COMPLETE SUMMARY

**Generated:** May 2026  
**Project:** Hệ thống Quản lý Dịch vụ & Bán hàng - Máy lọc nước An Gia (Water Filter Maintenance Management)  
**Duration:** 12 weeks  
**Team Size:** 6 people  
**Total Effort:** ~600 person-hours (72 person-days)

---

## 📚 DOCUMENTATION CREATED

### 1. **12WEEK_PROJECT_PLAN.md** ← START HERE
   - **For:** Project leads, stakeholders
   - **Contents:**
     - Detailed breakdown of all 12 weeks
     - 7 GO/NO-GO gates (critical milestones)
     - Risks & mitigation strategies
     - Resource allocation
     - Dependencies & blockers
   - **Length:** ~500 lines
   - **Most important sections:** Week 3-4 (auth), Week 7-8 (transactions), Week 9-10 (core business)

### 2. **DETAILED_TASK_ASSIGNMENT.md** ← FOR TEAM LEADS
   - **For:** Backend lead assigning work to team
   - **Contents:**
     - Hour-by-hour task breakdown for all 12 weeks
     - Task assignments by person (BL, SBD1, SBD2, MBD1, MBD2, QA)
     - Estimation for each task
     - Weekly summary by person
     - Critical path identification
   - **Length:** ~400 lines
   - **How to use:** Copy-paste into project management tool (Jira/Azure DevOps)

### 3. **QUICK_REFERENCE_CHECKLIST.md** ← FOR DAILY STANDUPS
   - **For:** Developers, QA, daily progress tracking
   - **Contents:**
     - 7 project gates (copy to whiteboard)
     - Weekly status template
     - Critical authorization matrix
     - Transaction testing checklist
     - Smoke test script
     - Team standup format
   - **Length:** ~350 lines
   - **How to use:** Print & post in team room, reference daily

### 4. **VISUAL_ROADMAP.md** ← FOR EXECUTIVE DASHBOARD
   - **For:** Managers, stakeholders, visual learners
   - **Contents:**
     - GANTT chart showing parallel work streams
     - Dependency graph (critical path)
     - Team capacity allocation by week
     - Risk timeline
     - Burn-down chart
     - Milestone dates
   - **Length:** ~400 lines
   - **How to use:** Share in meetings, update weekly as progress tracker

---

## 🎯 KEY FINDINGS FROM ANALYSIS

### Project Complexity: **HIGH**
- **Why:** Multiple state machines (Orders, Bookings), transactional integrity requirements, role-based data-level filtering
- **Implication:** Needs senior developers on critical path, cannot rush testing

### Critical Vulnerabilities to Fix: **9 findings** (3 CRITICAL, 3 HIGH, 2 MEDIUM, 1 INFO)
- Hardcoded DB password & JWT secret
- Weak cookie security
- Missing password policy
- Insufficient exception handling

### Risk Zones (Highest → Lowest)
1. 🔴 **Week 7-8 (Orders)** — Transaction atomicity, stock management
2. 🔴 **Week 9-10 (Bookings)** — Core business logic, data-level auth
3. 🟠 **Week 3-4 (Auth)** — JWT + Spring Security integration
4. 🟠 **Week 12 (Testing)** — Late discovery of integration bugs
5. 🟡 **Week 11 (PayPal)** — Third-party API integration

---

## 📊 PROJECT STRUCTURE AT A GLANCE

```
PHASE 1: FOUNDATION (Week 1-2)
├─ Database schema (11 tables)
├─ JPA entities & repositories
├─ Exception handling framework
└─ DELIVER: Database ready ✅

PHASE 2: SECURITY (Week 3-4)
├─ JWT authentication (Nimbus + Spring OAuth2)
├─ User login/refresh/logout flow
├─ Role-based access control
└─ DELIVER: Auth system working ✅

PHASE 3: CORE FEATURES (Week 5-12) — PARALLEL
├─ Product Management (Week 5-6)
├─ Order Management (Week 7-8) ⭐
├─ Maintenance Booking (Week 9-10) ⭐
└─ PayPal Integration (Week 11)
    └─ DELIVER: Full API ready ✅

PHASE 4: QUALITY & DEPLOYMENT (Week 12)
├─ 60%+ test coverage
├─ Security hardening
├─ Documentation
└─ DELIVER: Production-ready backend ✅
```

---

## 🚀 QUICK START (Week 1)

### Day 1 Monday
- [ ] All team reads `12WEEK_PROJECT_PLAN.md`
- [ ] Assign tasks from `DETAILED_TASK_ASSIGNMENT.md`
- [ ] Setup **daily** 15-min standup (use `QUICK_REFERENCE_CHECKLIST.md` template)
- [ ] Print & post `VISUAL_ROADMAP.md` in team room

### Day 2-5 (Mon-Fri)
- [ ] SBD1 starts on Entity layer (Role → RefreshToken)
- [ ] SBD2 starts on Config + Exception handling
- [ ] MBD1 starts on ApiResponse + Enums + Custom Exceptions
- [ ] QA prepares MySQL environment + test data
- [ ] BL reviews & approves database schema

### Friday EOD (Day 5)
- [ ] GATE 1 CHECK: Database connects, entities compile
- [ ] If ❌ FAIL: Don't proceed to Week 2, fix and retry

---

## ⚠️ CRITICAL SUCCESS FACTORS

| Factor | Why | Action |
|---|---|---|
| **GATE enforcement** | Prevents downstream failures | Don't skip a GATE, repeat week if needed |
| **Transaction testing** | Prevents data corruption | Start Week 8 tests on Monday, not Friday |
| **Data-level auth** | Prevents security breach | Test every role × endpoint, not just happy path |
| **Code review** | Prevents bugs & debt | Every PR reviewed before merge, no shortcuts |
| **Documentation** | Prevents knowledge loss | Update docs as code changes, not at end |
| **Communication** | Prevents coordination issues | Daily 15-min standup, weekly status update |

---

## 📈 SUCCESS METRICS (Week 12 Target)

```
✅ CODE METRICS
   Compilation:        100% success (0 errors)
   Warnings:           < 5 total
   Test coverage:      >= 60% (target 70%)
   Code review:        100% of PRs reviewed

✅ QUALITY GATES
   GATE 1-7:           All 7 gates passed
   Critical path:      No delays
   Integration tests:  100% pass
   Authorization:      Data-level filtering verified

✅ SECURITY
   Critical vulns:     0 (target: 0)
   High vulns:         0 (target: 0)
   Security audit:     All findings resolved
   Secrets:            0 hardcoded values

✅ DEPLOYMENT READY
   Environment vars:   All externalized
   Database:           Migration script ready
   Documentation:      README + Swagger + Postman
   Staging test:       Successful
```

---

## 🎓 HOW TO USE EACH DOCUMENT

### **For Project Lead:**
```
Week 1: Read 12WEEK_PROJECT_PLAN.md, understand 7 gates
Weekly: Check VISUAL_ROADMAP.md for critical path, update burn-down
Week 12: Review all GATE 7 sign-off criteria
```

### **For Backend Lead (Architect):**
```
Week 1: Review DETAILED_TASK_ASSIGNMENT.md allocations
Daily: Lead standup using QUICK_REFERENCE_CHECKLIST.md template
Weekly: Verify GATE criteria with team, approve architecture decisions
Week 7-8, 9-10: Extra focus on transaction logic & auth (high risk)
Week 12: Lead code review & final security audit
```

### **For Developers:**
```
Day 1: Get task assignment from DETAILED_TASK_ASSIGNMENT.md
Daily: Check QUICK_REFERENCE_CHECKLIST.md progress + blockers
Weekly: Update status in "Weekly Status Template" section
Week 7-8, 9-10: Extra testing rigor (high criticality)
Week 12: Support QA in integration testing
```

### **For QA:**
```
Week 1: Setup MySQL environment, prepare test data
Week 3-4: Execute auth flow tests from QUICK_REFERENCE_CHECKLIST.md
Week 8: Run transaction tests (CRITICAL!)
Week 10: Run authorization matrix tests
Week 12: Lead smoke tests + staging deployment
```

---

## 🛠️ TECHNOLOGY DECISIONS (Non-negotiable)

| Decision | Why | Not this |
|---|---|---|
| **JWT Library:** Nimbus (Spring OAuth2) | Spring native, no external deps | ❌ jjwt, jsonwebtoken |
| **Secrets:** Environment variables | Secure, externalized | ❌ application.properties |
| **Auth:** Spring Security OAuth2 Resource Server | Standard, no custom filters | ❌ Custom JWT filters |
| **ORM:** Spring Data JPA + Hibernate | Auto-management, no tuning | ❌ Raw JDBC |
| **Transactions:** `@Transactional(rollbackFor=Exception.class)` | Atomic or nothing | ❌ Manual commit/rollback |
| **Response wrapper:** `ApiResponse<T>` | Consistent schema | ❌ Plain response objects |
| **Soft delete:** `is_deleted=false` filter | Data retention | ❌ Hard delete |
| **Role-based auth:** @PreAuthorize + Service layer filter | Two-layer defense | ❌ Controller only |

---

## 📞 KEY CONTACTS / APPROVAL CHAIN

**Recommended team:**

```
Project Sponsor / Manager
    ↓
Backend Lead (Architecture decision maker)
  ├─ SBD1 (Entity/Service/Transaction expert)
  ├─ SBD2 (Security/JWT expert)
  ├─ MBD1 (DTO/integration coding)
  ├─ MBD2 (Controller coding)
  └─ QA (Testing & deployment)
```

**Decision escalation:**
- Architecture issues → Backend Lead
- Transaction spaghetti → SBD1
- JWT/Security issues → SBD2  
- Authorization bypass → SBD2 + Backend Lead
- Integration test failures → QA + MBD2
- Timeline risks → Project Manager + Backend Lead

---

## 🎁 DELIVERABLES SUMMARY

| Week | What ships | Quality gate |
|---|---|---|
| Week 2 | Database + Entities | Compile + connect ✅ |
| Week 4 | Auth system | Login/logout works ✅ |
| Week 6 | Products API | CRUD + filters ✅ |
| Week 8 | Orders system | Transactions atomic ✅ |
| Week 10 | Bookings (core) | State machine solid ✅ |
| Week 11 | Payment integration | Sandbox mode OK ✅ |
| Week 12 | Production release | 0 vuln, 60%+ tests ✅ |

---

## ⏱️ TIMELINE AT A GLANCE

```
Start:                    Week 1 Monday
├─ Foundation:            Week 1-2 (Friday GATE 1)
├─ Security:              Week 3-4 (Friday GATE 2)
├─ Product features:      Week 5-6 + 11 (parallel)
├─ Order transactions:    Week 7-8 (Friday GATE 4 — CRITICAL!)
├─ Booking core:          Week 9-10 (Friday GATE 5 — CORE BUSINESS!)
├─ PayPal (parallel):     Week 11 (Friday GATE 6)
├─ Testing/hardening:     Week 12 (Friday GATE 7)
└─ End:                   Week 12 Friday ✅

Total: 84 calendar days (including weekends slack)
Sprint: 12 weeks × 5 days = 60 working days
Contingency: ~2 weeks buffer recommended (total 14 weeks realistically)
```

---

## 🚨 RED FLAGS (Stop & Escalate If...)

```
STOP if:
  ❌ Week 2 Friday: Database NOT ready (blocks everything)
  ❌ Week 4 Friday: JWT login NOT working (blocks auth testing)
  ❌ Week 8 Friday: Transaction tests FAIL (data corruption risk)
  ❌ Week 10 Friday: Booking state machine BROKEN (core business down)
  ❌ Week 12: 50%+ test failures found (quality gate fail)
  ❌ Security audit: 2+ critical vulns remaining (deployment blocked)

ACTION: Repeat the week, don't proceed forward!
```

---

## 📊 COMPARISON: Estimated vs. Reality

```
Optimistic scenario (85% delivery):
  → May miss PayPal (Week 11) or advanced features
  → Tight testing schedule Week 12
  → Probability: 15% (only with perfect team + no interruptions)

Realistic scenario (100% delivery):
  → All 7 gates passed
  → Week 12 testing finds/fixes ~5 issues
  → 60%+ test coverage achieved
  → Probability: 70% (with good team + standard interruptions)

Pessimistic scenario (120% duration):
  → Week 8 transaction bugs require full repeat
  → Week 10 data-level auth bypass found late
  → Week 12 security audit finds critical issue
  → Extended to 14 weeks
  → Probability: 15% (delays cascade)

CONTINGENCY: Add +2 weeks for realistic schedule
Final realistic target: 14 weeks (not 12)
```

---

## 🎓 LEARNING OUTCOMES

By completing this project, your team will have expertise in:

✅ Spring Boot 3.5.11 enterprise patterns  
✅ JWT authentication + Spring Security OAuth2  
✅ Transactional database integrity  
✅ Role-based access control + data-level authorization  
✅ Swagger/OpenAPI documentation  
✅ PayPal payment integration  
✅ Complex state machines  
✅ MySQL optimization  
✅ Unit + integration testing best practices  
✅ Security audit remediation  

---

## 📄 DOCUMENT INDEX

| Document | Purpose | Audience | When to read |
|---|---|---|---|
| `12WEEK_PROJECT_PLAN.md` | Master plan | Leads, stakeholders | Week 1 |
| `DETAILED_TASK_ASSIGNMENT.md` | Task breakdown | Leads, developers | Week 1 (assign work) |
| `QUICK_REFERENCE_CHECKLIST.md` | Daily standup | All team | Daily (print & post) |
| `VISUAL_ROADMAP.md` | Timeline/dependencies | Managers, leads | Weekly review |
| `00_PROJECT_CONTEXT.md` | Requirements | All team | Week 1 (read once) |
| `01_DATABASE_SCHEMA.md` | Database design | SBD1, QA | Week 1-2 |
| `02_BUSINESS_LOGIC.md` | State machines | SBD1, developers | Week 7-8, 9-10 |
| `03_API_SPECS.md` | API endpoints | All developers | Week 5+ |
| `04_SECURITY.md` | JWT + auth patterns | SBD2, leads | Week 3-4, 12 |
| `05_CODING_STEPS.md` | Implementation guide | Developers | Each week |
| `06_PAYPAL.md` | Payment integration | MBD2 | Week 11 |
| `CONTROLLER_FLOW.md` | Architecture reference | Leads, architects | Week 5+ |
| `security_audit_report.md` | Vulnerabilities | SBD2, leads | Week 3, 12 |

---

## ✅ FINAL CHECKLIST

**Before project starts:**
- [ ] All team members have read `12WEEK_PROJECT_PLAN.md`
- [ ] Tasks assigned from `DETAILED_TASK_ASSIGNMENT.md`
- [ ] Daily standup scheduled (use `QUICK_REFERENCE_CHECKLIST.md`)
- [ ] `VISUAL_ROADMAP.md` printed and posted
- [ ] MySQL environment ready (SBD1 + QA)
- [ ] Git repo initialized with branches strategy
- [ ] Jira/Azure DevOps board created
- [ ] Communication channel setup (Slack/Teams)
- [ ] Backup/recovery plan documented
- [ ] Project sponsor sign-off confirmed

**Before Week 12 deployment:**
- [ ] All 7 GATES passed
- [ ] 0 critical vulnerabilities
- [ ] >= 60% test coverage
- [ ] Staging deployment successful
- [ ] Runbook documented
- [ ] Rollback plan prepared
- [ ] Team trained on deployment procedure

---

## 📞 QUESTIONS?

For clarification on any aspect of this plan:
1. **Architecture**: Contact Backend Lead or SBD1
2. **Security**: Contact SBD2
3. **Implementation details**: Contact specific owner (see task assignment)
4. **Timeline/scope**: Contact Project Manager or Backend Lead
5. **Testing strategy**: Contact QA
6. **Documentation**: Contact Backend Lead

---

**🎉 Project Plan Ready for Execution!**

**Status:** APPROVED FOR START  
**Version:** 1.0  
**Date:** May 2026  
**Prepared by:** GitHub Copilot (Architecture Analysis)  
**Expected completion:** Week 12 Friday (or Week 14 with contingency)

---

## 🔗 QUICK LINKS

- [📅 Full 12-Week Plan](12WEEK_PROJECT_PLAN.md)
- [👥 Task Assignments](DETAILED_TASK_ASSIGNMENT.md)
- [✅ Daily Checklist](QUICK_REFERENCE_CHECKLIST.md)
- [📊 Visual Roadmap](VISUAL_ROADMAP.md)

**Good luck with your project! 🚀**
