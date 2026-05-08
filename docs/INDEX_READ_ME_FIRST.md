# 📑 ANGIA BACKEND 12-WEEK PLAN - COMPLETE FOLDER INDEX

**📍 Location:** `s:\angia_website\angia-backend\docs\`

Generated: May 2026  
All documents ready for team distribution

---

## 📚 YOUR 5 NEW PLANNING DOCUMENTS (READ IN THIS ORDER)

### 1️⃣ **PROJECT_PLAN_SUMMARY.md** ← START HERE FIRST
   - **Read time:** 10 minutes
   - **For:** Everyone (executive summary)
   - **Contains:** Overview of all 4 documents, 7 gates, timeline, quick start guide
   - **Best for:** First-time orientation, showing stakeholders

### 2️⃣ **12WEEK_PROJECT_PLAN.md** ← DETAILED ROADMAP
   - **Read time:** 30 minutes
   - **For:** Project leads, backend lead, team planning
   - **Contains:** Week-by-week breakdown, risks, resource allocation, success criteria
   - **Best for:** Understand full scope, identify critical dates

### 3️⃣ **DETAILED_TASK_ASSIGNMENT.md** ← TASK ALLOCATION
   - **Read time:** 20 minutes (then reference weekly)
   - **For:** Backend lead, project managers (task assignment)
   - **Contains:** Specific tasks with owners, hours, week-by-week allocation
   - **Best for:** Assigning work to team members, tracking person-hours

### 4️⃣ **VISUAL_ROADMAP.md** ← EXECUTIVE DASHBOARD
   - **Read time:** 15 minutes
   - **For:** Managers, stakeholders, visual learners (weekly reviews)
   - **Contains:** GANTT chart, dependency graph, capacity allocation, risk timeline
   - **Best for:** Team updates, Gantt visualization, identifying risks
   - **Action:** Print & post in team room

### 5️⃣ **QUICK_REFERENCE_CHECKLIST.md** ← DAILY STANDUP GUIDE
   - **Read time:** 5 minutes (but print it!)
   - **For:** All team members, daily use
   - **Contains:** Weekly checklists, authorization matrix, smoke tests, standup template
   - **Best for:** Daily standups, progress tracking, blocking issues
   - **Action:** Print & post in team room for everyone

---

## 🗂️ HOW DOCUMENTS RELATE

```
┌─────────────────────────────────────────────────────┐
│ PROJECT_PLAN_SUMMARY.md                             │
│ (Overview & quick start — read this first!)         │
└──────────────┬──────────────────────────────────────┘
               │ Points to ↓
      ┌────────┴────────┬────────┬──────────┐
      │                 │        │          │
      ▼                 ▼        ▼          ▼
  12WEEK_PLAN.md  TASK_ASSIGN  ROADMAP   CHECKLIST
  (7 gates)       (6 people)    (timeline)  (daily)
   └─ Read in     └─ Use for    └─ Print   └─ Print
      Week 1        planning       weekly      daily
      once          tasks

All documents reference each other's key sections.
```

---

## 📖 RECOMMENDED READING SCHEDULE

### **Before Project Starts (Week 0)**
- [ ] Project manager: Read `PROJECT_PLAN_SUMMARY.md` (10 min)
- [ ] Backend lead: Read `PROJECT_PLAN_SUMMARY.md` + `12WEEK_PROJECT_PLAN.md` (40 min)
- [ ] Team leads: Read all 5 documents (2 hours)
- [ ] All team: Read `PROJECT_PLAN_SUMMARY.md` (10 min)

### **Week 1 Monday (First day)**
- [ ] All team: 1-hour orientation meeting
  - Overview: `PROJECT_PLAN_SUMMARY.md`
  - Timeline: `VISUAL_ROADMAP.md` (show printed version)
  - Tasks: `DETAILED_TASK_ASSIGNMENT.md` (assign tasks to each person)
  - Daily usage: `QUICK_REFERENCE_CHECKLIST.md` (give everyone a copy)

### **Daily (Mon-Fri, all weeks)**
- [ ] Each person: Reference own tasks in `DETAILED_TASK_ASSIGNMENT.md`
- [ ] Team: 15-min standup using `QUICK_REFERENCE_CHECKLIST.md` template
- [ ] Lead: Check `VISUAL_ROADMAP.md` for critical path risks

### **Weekly (Every Friday EOD)**
- [ ] Project manager: Update `VISUAL_ROADMAP.md` burn-down chart
- [ ] Team: Update progress in shared tracker
- [ ] Lead: Check GATE criteria vs. actual progress

### **Critical moments**
- [ ] End Week 2: Verify GATE 1 checkpoint via `QUICK_REFERENCE_CHECKLIST.md`
- [ ] End Week 4: Verify GATE 2 checkpoint
- [ ] End Week 8: CRITICAL — Verify GATE 4 (transactions)
- [ ] End Week 10: CRITICAL — Verify GATE 5 (bookings)
- [ ] End Week 12: Verify GATE 7 (production ready)

---

## 🎯 USING EACH DOCUMENT IN PRACTICE

### **PROJECT_PLAN_SUMMARY.md**
```
Scenario: "I'm new to this project"
→ Read Project_Plan_Summary.md (covers everything briefly)

Scenario: "I need to show the board our timeline"
→ Use VISUAL_ROADMAP.md GANTT chart
```

### **12WEEK_PROJECT_PLAN.md**
```
Scenario: "What happens in Week 8?"
→ Search for "TUẦN 8" or "Week 8" in 12WEEK_PROJECT_PLAN.md
→ Get detailed breakdown of all tasks

Scenario: "What's the biggest risk?"
→ See "⚠️ RISKS & MITIGATION" section
→ Week 7-8 transactions have highest risk
```

### **DETAILED_TASK_ASSIGNMENT.md**
```
Scenario: "I'm SBD1, what's my job Week 7?"
→ Find "TUẦN 7" section
→ Look for rows with "SBD1" owner
→ Get specific task breakdown with hours

Scenario: "I need to allocate team for next week"
→ Use the "weekly workload per person" table
→ Assign based on capacity
```

### **VISUAL_ROADMAP.md**
```
Scenario: "Show me the critical path"
→ See "DEPENDENCY GRAPH" section
→ Database → Auth → Orders → Bookings → Deployment

Scenario: "What if we're falling behind?"
→ Check "BURN-DOWN CHART" section
→ Compare ideal line vs actual line
→ Assess if we can catch up

Scenario: "What's the Gantt chart?"
→ Print "GANTT CHART" section
→ Post in team room to show parallel work streams
```

### **QUICK_REFERENCE_CHECKLIST.md**
```
Scenario: "Daily standup tomorrow"
→ Copy "WEEKLY STATUS TEMPLATE" section
→ Print "7 GATES CHECKLIST"
→ Use for 15-min standup meeting

Scenario: "Test authorization, but I'm unsure what to test"
→ Find "AUTHORIZATION MATRIX" section
→ Every row = one test case
→ Follow and mark as tested

Scenario: "Need a smoke test script"
→ Find "END-TO-END SMOKE TEST Week 12" section
→ Copy the bash script
→ Run it before deploying
```

---

## 🔗 CROSS-REFERENCES (How to navigate between documents)

**Question: "What gets done in Week 8?"**
1. Check PROJECT_PLAN_SUMMARY.md (overview) → points to 12WEEK_PROJECT_PLAN.md
2. Open 12WEEK_PROJECT_PLAN.md → find "TUẦN 8" section
3. Get specific tasks → open DETAILED_TASK_ASSIGNMENT.md for hours/owners
4. See capacity needed → check VISUAL_ROADMAP.md for team allocation

**Question: "Why is Week 8 critical?"**
1. Skim QUICK_REFERENCE_CHECKLIST.md (reason listed)
2. Read 12WEEK_PROJECT_PLAN.md "⚠️ RISKS" section
3. Check VISUAL_ROADMAP.md "RISK TIMELINE"

**Question: "Did we pass GATE 4?"**
1. QUICK_REFERENCE_CHECKLIST.md → "GATES PASSED" section (checklist)
2. VISUAL_ROADMAP.md → "MILESTONE TIMELINE" (dates)
3. DETAILED_TASK_ASSIGNMENT.md → "WEEK 8" (tasks that were due)

---

## 📋 PRINTING & POSTING

### **For Team Room (Print these)**
- [ ] `VISUAL_ROADMAP.md` "GANTT CHART" (A1 size if possible)
- [ ] `QUICK_REFERENCE_CHECKLIST.md` "7 GATES PASSED" checklist
- [ ] `QUICK_REFERENCE_CHECKLIST.md` "WEEKLY STATUS TEMPLATE" (copy for each week)
- [ ] `QUICK_REFERENCE_CHECKLIST.md` "REFERENCE DOCUMENTS PIN TO SLACK"

### **For Each Team Member (Give copies)**
- [ ] `PROJECT_PLAN_SUMMARY.md` (read once, keep for reference)
- [ ] `QUICK_REFERENCE_CHECKLIST.md` (print, keep on desk)
- [ ] Their specific tasks from `DETAILED_TASK_ASSIGNMENT.md` (personalized)

### **For SharePoint/Wiki/Knowledge Base**
- [ ] Upload all 5 documents
- [ ] Create a "12-Week Plan" folder with index
- [ ] Link from project homepage

### **For Meetings**
- [ ] Weekly standup: Show `VISUAL_ROADMAP.md` burn-down chart
- [ ] Monthly review: Show `DETAILED_TASK_ASSIGNMENT.md` progress
- [ ] Quarterly: Show all documents to stakeholders

---

## 🎯 KEY TAKEAWAYS

### **The 7 Gates (Must-Pass Checkpoints)**
1. **GATE 1** (Week 2): Database ready ✅
2. **GATE 2** (Week 4): Auth working ✅
3. **GATE 3** (Week 6): Products CRUD ✅
4. **GATE 4** (Week 8): Transactions atomic ⭐ **CANNOT SKIP**
5. **GATE 5** (Week 10): Bookings working ⭐ **CANNOT SKIP**
6. **GATE 6** (Week 11): PayPal ready ✅
7. **GATE 7** (Week 12): Production ready ✅

### **The 3 Highest Risk Areas**
1. Week 7-8: Order transactions (atomicity)
2. Week 9-10: Booking state machine & data-level auth
3. Week 12: Late integration bugs

### **The Critical Path (Blocks everything else)**
Database → Auth → Orders → Bookings → Deployment
(Everything else can shift 1-2 weeks)

### **Success = Execution of Plan**
- 💯 Follow the plan exactly (don't skip steps)
- 🧪 Test transaction logic thoroughly (Week 8)
- 🔐 Enforce authorization rules (Week 10)
- 📋 Don't combine GATE checks (do them separately, on time)
- 🚨 If GATE fails, repeat that week (don't proceed)

---

## 📞 WHO SHOULD READ WHAT

| Role | Documents | Action |
|---|---|---|
| **Project Manager** | Summary + Roadmap | Update burn-down weekly |
| **Backend Lead** | All 5 documents | Make architecture decisions, review code |
| **SBD1** (Entity layer) | Plan + Tasks (own rows) | Execute entity + service layer |
| **SBD2** (Security) | Plan + Tasks (own rows) | Implement JWT + security config |
| **MBD1** (DTO/Product) | Plan + Tasks (own rows) | Code DTOs, Product controller |
| **MBD2** (Customer/Order) | Plan + Tasks (own rows) | Code Order/Booking controllers |
| **QA Engineer** | Checklist + Roadmap | Execute test plan weekly |
| **Stakeholder** | Summary + Roadmap | See timeline & risks |

---

## 🚀 NEXT STEPS (Do this now!)

- [ ] **Read:** `PROJECT_PLAN_SUMMARY.md` right now (10 min)
- [ ] **Share:** Send all 5 documents to your team
- [ ] **Schedule:** 1-hour orientation meeting for Week 1 Monday
- [ ] **Print:** `VISUAL_ROADMAP.md` + `QUICK_REFERENCE_CHECKLIST.md`
- [ ] **Assign:** Tasks from `DETAILED_TASK_ASSIGNMENT.md` to each person
- [ ] **Start:** Week 1 with database setup (SBD1 + QA)
- [ ] **Track:** Weekly using `VISUAL_ROADMAP.md` burn-down

---

## ❓ FAQ

**Q: Can we skip any weeks?**  
A: No. Critical path is: Database → Auth → Orders (Week 8 MUST PASS) → Bookings (Week 10 MUST PASS) → Deployment.

**Q: What if we're falling behind?**  
A: Check VISUAL_ROADMAP.md to see if you're on slack path. If on critical path, you've delayed the whole project.

**Q: What if GATE 4 (Week 8) fails?**  
A: Repeat Week 8 until transaction tests pass. Cannot proceed to Week 9.

**Q: Should we combine weeks?**  
A: No. Each week has sequential dependencies. Parallelizing can introduce bugs.

**Q: Can we hire more people to go faster?**  
A: Only for non-critical path (Products, PayPal). Critical path (Orders, Bookings) needs senior devs and can't parallelize.

**Q: What if we only have 4 people, not 6?**  
A: Extend timeline to 16-18 weeks, reduce parallelization. Assign 2x work per person.

**Q: How do we know if we're on track?**  
A: Every Friday, check QUICK_REFERENCE_CHECKLIST.md GATE criteria. If ✅, proceed to next week. If ❌, repeat week.

---

## 📄 DOCUMENT VERSIONS

All documents created: May 2026  
Version: 1.0 (Initial release)

**If you find an issue:**
1. Specific task clarification → email Backend Lead
2. Timeline question → email Project Manager
3. Technical concern → email relevant SBD/MBD

---

## 🎓 FINAL NOTE

**This is a realistic, achievable plan.** It's based on:
- ✅ Actual requirements from existing specs
- ✅ Spring Boot production patterns
- ✅ Typical team dynamics
- ✅ Buffer for common mistakes

**Follow it carefully.** The biggest risk is:**skipping GATE checks** or combining phases.

**You've got this!** 🚀

---

**Version:** 1.0  
**Created:** May 2026  
**Status:** READY FOR PROJECT START  
**Distribution:** Share all 5 docs with team immediately
