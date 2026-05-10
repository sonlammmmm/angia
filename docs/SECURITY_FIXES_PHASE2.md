# Security Fixes - Phase 2 (Completed)

## Overview
This document details all security fixes implemented in Phase 2 of the security audit response.

## Changes Made

### 1. Password Complexity Validation
**File**: `src/main/java/vn/dichvuangia/management/service/UserService.java`, `src/main/java/vn/dichvuangia/management/service/CustomerService.java`
- Implemented a rigorous password complexity check in both `UserService` and `CustomerService`.
- Passwords must now contain at least 8 characters, including uppercase, lowercase, numbers, and special characters.

### 2. Rate Limiting for Auth Endpoints
**File**: `src/main/java/vn/dichvuangia/management/security/RateLimitFilter.java`, `src/main/java/vn/dichvuangia/management/security/RateLimitService.java`
- Upgraded the global rate limiting to be context-aware based on the URI.
- Specific limits applied:
  - `/auth/login`: 5 requests per minute.
  - `/auth/register`: 3 requests per minute.
- Impact: Effectively mitigates brute-force attacks on authentication endpoints.

### 3. File Access Authorization
**File**: `src/main/java/vn/dichvuangia/management/security/SecurityConfig.java`
- Removed `.permitAll()` for `/files/**`.
- The `/files/**` endpoints now require a valid JWT authentication, restricting access to authenticated users only.

### 4. Logging Level Update
**File**: `src/main/resources/application.properties`
- Changed default application and Spring Security logging levels from `DEBUG` to `WARN`.
- Impact: Prevents accidental sensitive data leakage via excessive logging in production.

### 5. CSRF Protection for State-Changing Operations
**File**: `src/main/java/vn/dichvuangia/management/security/SecurityConfig.java`
- Replaced the disabled CSRF configuration with a robust setup utilizing `CookieCsrfTokenRepository.withHttpOnlyFalse()`.
- Excluded public endpoints, webhooks, and auth APIs (`/auth/**`, `/paypal/**`, `/guest/**`, `/ws/**`) from CSRF verification to maintain integration compatibility while protecting state-changing actions.

## Deployment Checklist Update
- [x] Apply Phase 2 code changes.
- [ ] Run security regression tests locally.
- [ ] Monitor rate-limit hits to fine-tune the thresholds if necessary.
