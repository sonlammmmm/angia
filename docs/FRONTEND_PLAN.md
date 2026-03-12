# Kế Hoạch Frontend React — AnGia Management System

> **Stack:** React 18 + TypeScript + Vite + React Router v6 + React Query + Zustand + Tailwind CSS

---

## 1. Khởi Tạo Dự Án

### Yêu Cầu

```
Node.js >= 18.x
npm >= 9.x (hoặc yarn / pnpm)
```

### Tạo Project

```bash
npm create vite@latest angia-frontend -- --template react-ts
cd angia-frontend
npm install
```

### Cài Đặt Dependencies

```bash
# Routing
npm install react-router-dom

# Data fetching & caching
npm install @tanstack/react-query @tanstack/react-query-devtools

# State management
npm install zustand

# HTTP client
npm install axios

# UI Framework
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p

# UI Components (chọn 1)
npm install @headlessui/react @heroicons/react       # Headless UI + Heroicons
# HOẶC
npm install antd                                     # Ant Design (đầy đủ hơn)

# Form & Validation
npm install react-hook-form zod @hookform/resolvers

# Notifications
npm install react-hot-toast

# Date formatting
npm install date-fns

# Dev tools
npm install -D @types/node
```

### Cấu Hình `.env`

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

---

## 2. Cấu Trúc Thư Mục

```
src/
├── api/                    # Axios instance + API functions
│   ├── axiosClient.ts      # Axios config, interceptors
│   ├── authApi.ts
│   ├── userApi.ts
│   ├── customerApi.ts
│   ├── productApi.ts
│   ├── brandApi.ts
│   ├── serviceApi.ts
│   ├── orderApi.ts
│   └── bookingApi.ts
│
├── components/             # Shared/reusable components
│   ├── ui/                 # Primitive UI elements
│   │   ├── Button.tsx
│   │   ├── Input.tsx
│   │   ├── Select.tsx
│   │   ├── Modal.tsx
│   │   ├── Table.tsx
│   │   ├── Pagination.tsx
│   │   ├── Badge.tsx       # Status badges
│   │   └── Spinner.tsx
│   ├── layout/
│   │   ├── MainLayout.tsx  # Sidebar + Header + Outlet
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   └── AuthLayout.tsx  # Login/Register pages
│   └── ProtectedRoute.tsx  # Route guard theo role
│
├── features/               # Feature-based modules
│   ├── auth/
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   └── useAuth.ts
│   ├── dashboard/
│   │   └── DashboardPage.tsx
│   ├── users/
│   │   ├── UserListPage.tsx
│   │   ├── UserFormModal.tsx
│   │   └── userApi.ts (hoặc dùng api/ chung)
│   ├── customers/
│   │   ├── CustomerListPage.tsx
│   │   ├── CustomerDetailPage.tsx
│   │   ├── CustomerFormModal.tsx
│   │   └── MyProfilePage.tsx
│   ├── products/
│   │   ├── ProductListPage.tsx
│   │   ├── ProductDetailPage.tsx
│   │   └── ProductFormModal.tsx
│   ├── brands/
│   │   └── BrandListPage.tsx
│   ├── services/
│   │   └── ServiceListPage.tsx
│   ├── orders/
│   │   ├── OrderListPage.tsx
│   │   ├── OrderDetailPage.tsx
│   │   └── OrderCreatePage.tsx
│   └── bookings/
│       ├── BookingListPage.tsx
│       ├── BookingDetailPage.tsx
│       └── BookingCreatePage.tsx
│
├── store/                  # Zustand stores
│   └── authStore.ts        # User info, accessToken
│
├── types/                  # TypeScript type definitions
│   ├── auth.ts
│   ├── user.ts
│   ├── customer.ts
│   ├── product.ts
│   ├── brand.ts
│   ├── service.ts
│   ├── order.ts
│   ├── booking.ts
│   └── api.ts              # ApiResponse<T>, Page<T>
│
├── utils/
│   ├── formatters.ts       # Date, currency formatters
│   └── constants.ts        # App constants
│
├── hooks/
│   └── useDebounce.ts      # Search debounce
│
├── router/
│   └── index.tsx           # React Router config
│
├── App.tsx
└── main.tsx
```

---

## 3. TypeScript Types

### `src/types/api.ts`

```typescript
export interface ApiResponse<T> {
  status: 'success' | 'error';
  message: string;
  data: T | null;
  errorCode: string | null;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;       // current page (0-indexed)
  size: number;
}

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
}
```

### `src/types/auth.ts`

```typescript
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  fullName: string;
  phone: string;
  address?: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface TokenRefreshResponse {
  accessToken: string;
  tokenType: string;
  newRefreshToken: string;
}
```

### `src/types/user.ts`

```typescript
export interface UserResponse {
  id: number;
  username: string;
  roleName: 'ADMIN' | 'MANAGEMENT' | 'SALE' | 'TECHNICIAN' | 'CUSTOMER';
  isActive: boolean;
  createdAt: string;
}

export interface UserCreateRequest {
  username: string;
  password: string;
  roleId: number;
}

export interface UserUpdateRequest {
  password?: string;
  roleId?: number;
  isActive?: boolean;
}
```

### `src/types/order.ts`

```typescript
export type OrderStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'CANCELLED';

export interface OrderItemResponse {
  id: number;
  productId: number;
  productName: string;
  productCode: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface OrderResponse {
  id: number;
  orderCode: string;
  status: OrderStatus;
  totalAmount: number;
  shippingAddress: string;
  createdAt: string;
  updatedAt: string;
  customerId: number;
  customerName: string;
  customerPhone: string;
  saleId: number | null;
  saleUsername: string | null;
  items: OrderItemResponse[];
}

export interface OrderCreateRequest {
  customerId: number;
  shippingAddress: string;
  items: Array<{ productId: number; quantity: number }>;
}
```

### `src/types/booking.ts`

```typescript
export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';

export interface BookingResponse {
  id: number;
  bookingCode: string;
  status: BookingStatus;
  bookingDate: string;
  notes: string | null;
  createdAt: string;
  customerId: number;
  customerName: string;
  customerPhone: string;
  customerAddress: string | null;
  serviceId: number;
  serviceName: string;
  serviceBasePrice: number;
  technicianId: number | null;
  technicianUsername: string | null;
}
```

---

## 4. Axios Client

### `src/api/axiosClient.ts`

```typescript
import axios from 'axios';
import { useAuthStore } from '../store/authStore';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,   // QUAN TRỌNG: gửi HttpOnly Cookie refreshToken
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: gắn Access Token
axiosClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: auto-refresh khi 401
let isRefreshing = false;
let failedQueue: Array<{ resolve: Function; reject: Function }> = [];

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error);
    else resolve(token);
  });
  failedQueue = [];
};

axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return axiosClient(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const response = await axiosClient.post('/auth/refresh');
        const newToken = response.data.data.accessToken;
        useAuthStore.getState().setAccessToken(newToken);
        processQueue(null, newToken);
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as Error, null);
        useAuthStore.getState().logout();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default axiosClient;
```

---

## 5. Auth Store (Zustand)

### `src/store/authStore.ts`

```typescript
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { UserResponse } from '../types/user';

interface AuthState {
  accessToken: string | null;
  user: Pick<UserResponse, 'id' | 'username' | 'roleName'> | null;
  expiresAt: number | null;         // timestamp (ms) khi token hết hạn

  setAuth: (token: string, expiresIn: number, user: AuthState['user']) => void;
  setAccessToken: (token: string) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
  hasRole: (...roles: string[]) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,
      expiresAt: null,

      setAuth: (token, expiresIn, user) => set({
        accessToken: token,
        user,
        expiresAt: Date.now() + expiresIn * 1000,
      }),

      setAccessToken: (token) => set({ accessToken: token }),

      logout: () => set({ accessToken: null, user: null, expiresAt: null }),

      isAuthenticated: () => {
        const { accessToken, expiresAt } = get();
        return !!accessToken && !!expiresAt && Date.now() < expiresAt;
      },

      hasRole: (...roles) => {
        const { user } = get();
        return !!user && roles.includes(user.roleName);
      },
    }),
    {
      name: 'angia-auth',          // localStorage key
      partialize: (state) => ({    // KHÔNG lưu token vào localStorage (bảo mật)
        user: state.user,
        expiresAt: state.expiresAt,
      }),
    }
  )
);
```

> ⚠️ **Lưu ý bảo mật:** Access Token **không** lưu vào localStorage. Chỉ lưu trong memory (Zustand). Khi refresh trang, token được lấy lại từ `/auth/refresh` (dùng cookie).

---

## 6. React Router

### `src/router/index.tsx`

```tsx
import { createBrowserRouter, Navigate } from 'react-router-dom';
import MainLayout from '../components/layout/MainLayout';
import AuthLayout from '../components/layout/AuthLayout';
import ProtectedRoute from '../components/ProtectedRoute';

import LoginPage from '../features/auth/LoginPage';
import RegisterPage from '../features/auth/RegisterPage';
import DashboardPage from '../features/dashboard/DashboardPage';
import UserListPage from '../features/users/UserListPage';
import CustomerListPage from '../features/customers/CustomerListPage';
import MyProfilePage from '../features/customers/MyProfilePage';
import ProductListPage from '../features/products/ProductListPage';
import ProductDetailPage from '../features/products/ProductDetailPage';
import BrandListPage from '../features/brands/BrandListPage';
import ServiceListPage from '../features/services/ServiceListPage';
import OrderListPage from '../features/orders/OrderListPage';
import OrderCreatePage from '../features/orders/OrderCreatePage';
import OrderDetailPage from '../features/orders/OrderDetailPage';
import BookingListPage from '../features/bookings/BookingListPage';
import BookingCreatePage from '../features/bookings/BookingCreatePage';

export const router = createBrowserRouter([
  // ── Auth routes ─────────────────────────────────────────────────
  {
    element: <AuthLayout />,
    children: [
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
    ],
  },

  // ── App routes (requires authentication) ────────────────────────
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <MainLayout />,
        children: [
          { index: true, element: <Navigate to="/dashboard" replace /> },
          { path: '/dashboard', element: <DashboardPage /> },

          // Admin only
          {
            path: '/users',
            element: <ProtectedRoute roles={['ADMIN']} />,
            children: [{ index: true, element: <UserListPage /> }],
          },

          // Admin, Management, Sale
          {
            path: '/customers',
            element: <ProtectedRoute roles={['ADMIN', 'MANAGEMENT', 'SALE']} />,
            children: [
              { index: true, element: <CustomerListPage /> },
            ],
          },

          // Customer own profile
          { path: '/profile', element: <MyProfilePage /> },

          // Products — public view, manage needs role
          { path: '/products', element: <ProductListPage /> },
          { path: '/products/:id', element: <ProductDetailPage /> },

          // Brands, Services
          { path: '/brands', element: <BrandListPage /> },
          { path: '/services', element: <ServiceListPage /> },

          // Orders
          { path: '/orders', element: <OrderListPage /> },
          { path: '/orders/new', element: <OrderCreatePage /> },
          { path: '/orders/:id', element: <OrderDetailPage /> },

          // Bookings
          { path: '/bookings', element: <BookingListPage /> },
          { path: '/bookings/new', element: <BookingCreatePage /> },
        ],
      },
    ],
  },

  { path: '*', element: <Navigate to="/dashboard" replace /> },
]);
```

---

## 7. ProtectedRoute Component

### `src/components/ProtectedRoute.tsx`

```tsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

interface Props {
  roles?: string[];
}

export default function ProtectedRoute({ roles }: Props) {
  const { isAuthenticated, hasRole } = useAuthStore();

  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  if (roles && !hasRole(...roles)) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}
```

---

## 8. Sidebar Navigation theo Role

| Menu Item | Icon | Roles |
|---|---|---|
| Dashboard | 📊 | All |
| Quản lý người dùng | 👥 | ADMIN |
| Khách hàng | 🤝 | ADMIN, MANAGEMENT, SALE |
| Hồ sơ của tôi | 👤 | CUSTOMER |
| Sản phẩm | 📦 | All |
| Thương hiệu | 🏷️ | ADMIN, MANAGEMENT |
| Dịch vụ | 🔧 | All |
| Đơn hàng | 🛒 | All |
| Lịch bảo trì | 📅 | All |

---

## 9. Thứ Tự Implement

### Phase 1 — Core Foundation (Ngày 1-2)
- [x] Khởi tạo dự án Vite + TypeScript
- [ ] Cài đặt dependencies
- [ ] Cấu hình Tailwind CSS
- [ ] Tạo Axios client với interceptors
- [ ] Tạo Zustand auth store
- [ ] Tạo types/api.ts, types/auth.ts

### Phase 2 — Authentication (Ngày 2-3)
- [ ] LoginPage — form đăng nhập
- [ ] RegisterPage — form đăng ký
- [ ] AuthLayout (centered card layout)
- [ ] Auto-refresh token on app startup
- [ ] ProtectedRoute guard

### Phase 3 — Layout & Navigation (Ngày 3-4)
- [ ] MainLayout (Sidebar + Header)
- [ ] Sidebar với menu phân quyền
- [ ] Header (user info + logout)
- [ ] Responsive design (mobile/tablet)

### Phase 4 — Core Features (Ngày 4-10)
- [ ] Dashboard (thống kê nhanh)
- [ ] Products — list, detail, create/edit (ADMIN/MGMT)
- [ ] Brands — list, create/edit
- [ ] Services — list, create/edit
- [ ] Customers — list, detail, create/edit
- [ ] Orders — list, create, detail, update status
- [ ] Bookings — list, create, detail, assign, complete, cancel

### Phase 5 — ADMIN Features (Ngày 10-12)
- [ ] User management — list, create, lock/unlock
- [ ] My Profile (CUSTOMER)

### Phase 6 — Polish (Ngày 12-14)
- [ ] Loading states và Skeleton screens
- [ ] Error handling và toast notifications
- [ ] Form validation messages
- [ ] Responsive mobile optimization
- [ ] Empty states

---

## 10. Thư Viện Quan Trọng

### React Query — Data Fetching

```typescript
// Ví dụ: fetch danh sách sản phẩm
import { useQuery } from '@tanstack/react-query';
import { getProducts } from '../api/productApi';

export const useProducts = (params: ProductFilterParams) => {
  return useQuery({
    queryKey: ['products', params],
    queryFn: () => getProducts(params),
    staleTime: 5 * 60 * 1000,   // cache 5 phút
  });
};
```

### React Hook Form + Zod — Forms

```typescript
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const loginSchema = z.object({
  username: z.string().min(3, 'Tối thiểu 3 ký tự'),
  password: z.string().min(6, 'Tối thiểu 6 ký tự'),
});

type LoginFormData = z.infer<typeof loginSchema>;

const form = useForm<LoginFormData>({
  resolver: zodResolver(loginSchema),
});
```

---

## 11. Ghi Chú Quan Trọng

1. **`withCredentials: true`** trong Axios là **bắt buộc** để cookie `refreshToken` được gửi/nhận
2. Sau khi refresh trang, gọi `POST /auth/refresh` để lấy lại access token từ cookie
3. `accessToken` **không** lưu localStorage — chỉ trong Zustand memory
4. Mọi request cần `Authorization: Bearer <accessToken>` header
5. Khi nhận response 401, auto-refresh token trước khi retry request
6. CORS backend đã cấu hình cho `http://localhost:5173` (Vite default port)
