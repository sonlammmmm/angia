# Authentication Flow — AnGia Frontend (React + JWT + HttpOnly Cookie)

---

## 1. Tổng Quan

AnGia dùng chiến lược **Dual-Token Authentication**:

| Token | Lưu ở đâu | Thời hạn | Mục đích |
|---|---|---|---|
| **Access Token** (JWT) | Memory (Zustand) | 1 giờ | Xác thực mọi API request |
| **Refresh Token** (UUID) | HttpOnly Cookie | 7 ngày | Cấp lại access token |

**Tại sao không lưu access token trong localStorage?**
- localStorage bị đọc bởi JavaScript → dễ bị XSS đánh cắp
- HttpOnly Cookie không đọc được bởi JS → an toàn hơn với XSS

---

## 2. Luồng Đăng Ký

```
User nhập form RegisterPage
        ↓
POST /auth/register { username, password, fullName, phone, address }
        ↓
Backend:
  1. Kiểm tra username/phone trùng → 400 nếu trùng
  2. Tạo User (role CUSTOMER) + Customer profile
  3. Sinh access token + refresh token
  4. Set-Cookie: refreshToken=<uuid>; HttpOnly; SameSite=Lax; Path=/
  5. Response body: { accessToken, tokenType, expiresIn }
        ↓
Frontend:
  1. Lưu accessToken vào Zustand (memory)
  2. Decode JWT để lấy { userId, roleName, username }
  3. Lưu user info vào Zustand
  4. Redirect → /dashboard
```

---

## 3. Luồng Đăng Nhập

```
User nhập form LoginPage
        ↓
POST /auth/login { username, password }
        ↓
Backend:
  1. Spring Security xác thực username/password
  2. Nếu sai → 401 INVALID_CREDENTIALS
  3. Nếu bị khóa → 403 ACCOUNT_DISABLED
  4. Sinh access token + refresh token
  5. Set-Cookie: refreshToken=<uuid>; HttpOnly; SameSite=Lax
  6. Response: { accessToken, tokenType, expiresIn }
        ↓
Frontend:
  1. Lưu accessToken vào Zustand
  2. Gọi GET /users/me HOẶC decode JWT để lấy user info
  3. Redirect → /dashboard (hoặc trang trước đó)
```

**Lưu ý:** JWT payload chứa `userId` và `scope` (role). Có thể decode để lấy thông tin user mà không cần gọi thêm API.

```typescript
// Decode JWT payload (không cần verify signature ở client)
function decodeJwt(token: string) {
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  return JSON.parse(window.atob(base64));
}

// Payload structure:
// {
//   "iss": "vn.dichvuangia",
//   "sub": "admin",          ← username
//   "userId": 1,
//   "scope": "ROLE_ADMIN",
//   "iat": 1234567890,
//   "exp": 1234571490
// }
```

---

## 4. Luồng Refresh Token (Auto-Refresh)

```
Request API bị trả về 401 Unauthorized
        ↓
Axios response interceptor bắt lỗi
        ↓
POST /auth/refresh (Cookie refreshToken gửi tự động)
        ↓
Backend:
  1. Đọc refreshToken từ Cookie
  2. Tìm trong DB, kiểm tra hết hạn
  3. XÓA token cũ (Rotation strategy)
  4. Sinh access token MỚI + refresh token MỚI
  5. Set-Cookie: refreshToken=<new-uuid>; HttpOnly
  6. Response: { accessToken, tokenType, newRefreshToken }
        ↓
Frontend:
  1. Cập nhật accessToken mới vào Zustand
  2. Retry original request với token mới
  3. Nếu refresh cũng fail (401/cookie hết hạn) → logout + redirect /login
```

**Queue pattern:** Nếu nhiều requests cùng lúc bị 401, chỉ gọi refresh 1 lần, các request khác chờ queue.

---

## 5. Luồng Đăng Xuất

```
User click Logout
        ↓
POST /auth/logout (Cookie refreshToken gửi tự động)
        ↓
Backend:
  1. Tìm và xóa refreshToken khỏi DB
  2. Set-Cookie: refreshToken=; MaxAge=0 (clear cookie)
        ↓
Frontend:
  1. Xóa accessToken + user info khỏi Zustand
  2. Redirect → /login
```

---

## 6. Khôi Phục Session Khi Refresh Trang

Khi user refresh trang, access token trong memory (Zustand) bị mất. Cần khôi phục từ cookie.

```typescript
// src/App.tsx hoặc một component root
import { useEffect } from 'react';
import { useAuthStore } from './store/authStore';
import axiosClient from './api/axiosClient';

function App() {
  const { setAuth, logout } = useAuthStore();

  useEffect(() => {
    // Khi app khởi động, thử refresh token
    const initializeAuth = async () => {
      try {
        const response = await axiosClient.post('/auth/refresh');
        const { accessToken, expiresIn } = response.data.data;

        // Decode JWT để lấy user info
        const payload = decodeJwt(accessToken);
        const user = {
          id: payload.userId,
          username: payload.sub,
          roleName: payload.scope.replace('ROLE_', '') as UserRole,
        };

        setAuth(accessToken, expiresIn, user);
      } catch {
        // Cookie không tồn tại hoặc hết hạn → cần login lại
        logout();
      }
    };

    initializeAuth();
  }, []);

  return <RouterProvider router={router} />;
}
```

---

## 7. Triển Khai useAuth Hook

### `src/features/auth/useAuth.ts`

```typescript
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { login, register, logout } from '../../api/authApi';
import toast from 'react-hot-toast';

function decodeJwt(token: string) {
  try {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
  } catch {
    return null;
  }
}

export function useLogin() {
  const navigate = useNavigate();
  const { setAuth } = useAuthStore();

  return useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      const { accessToken, expiresIn } = data;
      const payload = decodeJwt(accessToken);
      if (payload) {
        setAuth(accessToken, expiresIn, {
          id: payload.userId,
          username: payload.sub,
          roleName: payload.scope.replace('ROLE_', ''),
        });
      }
      navigate('/dashboard');
    },
    onError: (error: any) => {
      const msg = error.response?.data?.message || 'Đăng nhập thất bại';
      toast.error(msg);
    },
  });
}

export function useRegister() {
  const navigate = useNavigate();
  const { setAuth } = useAuthStore();

  return useMutation({
    mutationFn: register,
    onSuccess: (data) => {
      const { accessToken, expiresIn } = data;
      const payload = decodeJwt(accessToken);
      if (payload) {
        setAuth(accessToken, expiresIn, {
          id: payload.userId,
          username: payload.sub,
          roleName: payload.scope.replace('ROLE_', ''),
        });
      }
      toast.success('Đăng ký thành công!');
      navigate('/dashboard');
    },
    onError: (error: any) => {
      const msg = error.response?.data?.message || 'Đăng ký thất bại';
      toast.error(msg);
    },
  });
}

export function useLogout() {
  const navigate = useNavigate();
  const { logout: clearAuth } = useAuthStore();

  return useMutation({
    mutationFn: logout,
    onSettled: () => {
      clearAuth();
      navigate('/login');
    },
  });
}
```

---

## 8. API Functions

### `src/api/authApi.ts`

```typescript
import axiosClient from './axiosClient';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';
import type { ApiResponse } from '../types/api';

export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  const res = await axiosClient.post<ApiResponse<AuthResponse>>('/auth/login', data);
  return res.data.data!;
};

export const register = async (data: RegisterRequest): Promise<AuthResponse> => {
  const res = await axiosClient.post<ApiResponse<AuthResponse>>('/auth/register', data);
  return res.data.data!;
};

export const logout = async (): Promise<void> => {
  await axiosClient.post('/auth/logout');
};

export const refreshToken = async (): Promise<AuthResponse> => {
  const res = await axiosClient.post<ApiResponse<AuthResponse>>('/auth/refresh');
  return res.data.data!;
};
```

---

## 9. Xử Lý Lỗi Authentication

| HTTP | errorCode | Xử lý phía Frontend |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Hiển thị lỗi từng field trong form |
| 400 | `BAD_REQUEST` | Toast error message |
| 401 | `INVALID_CREDENTIALS` | "Tên đăng nhập hoặc mật khẩu không đúng" |
| 401 | `UNAUTHORIZED` | Auto-refresh → nếu fail thì redirect /login |
| 403 | `ACCOUNT_DISABLED` | "Tài khoản đã bị khóa, liên hệ admin" |
| 403 | `ACCESS_DENIED` | Hiển thị trang 403 hoặc redirect dashboard |

---

## 10. Sơ Đồ Tổng Quan

```
┌─────────────────────────────────────────────────────────────────┐
│                         Browser                                  │
│                                                                  │
│  ┌─────────────────────┐    ┌───────────────────────────────┐   │
│  │   Zustand Store      │    │   HttpOnly Cookie             │   │
│  │   (Memory only)      │    │   (Automatic, not readable    │   │
│  │                      │    │    by JavaScript)             │   │
│  │  accessToken: "eyJ.."│    │                               │   │
│  │  user: { id, role }  │    │  refreshToken: "uuid-abc-123" │   │
│  │  expiresAt: timestamp│    │                               │   │
│  └──────────┬───────────┘    └───────────────────────────────┘   │
│             │                               │ auto-sent           │
│             │ Bearer Token                  │ with every request  │
│             ↓                               ↓                     │
│  ┌─────────────────────────────────────────────────────────┐     │
│  │              Axios Client (withCredentials: true)        │     │
│  │                                                         │     │
│  │   Request Interceptor: add Authorization header         │     │
│  │   Response Interceptor: catch 401, auto-refresh         │     │
│  └─────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                          │
                          ↓ HTTP Requests
┌─────────────────────────────────────────────────────────────────┐
│                 Spring Boot Backend                               │
│                                                                  │
│  POST /auth/login  → Response: accessToken + Set-Cookie          │
│  POST /auth/refresh → Response: new accessToken + Set-Cookie     │
│  POST /auth/logout  → Clear Cookie                               │
│  GET  /api/...     → Validate Bearer Token (JWT)                 │
└─────────────────────────────────────────────────────────────────┘
```
