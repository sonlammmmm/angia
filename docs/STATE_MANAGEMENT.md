# State Management Guide — AnGia Frontend

> **Công cụ sử dụng:** Zustand (global state) + React Query (server state)

---

## 1. Chiến Lược Quản Lý State

AnGia dùng **hai loại state** với công cụ khác nhau:

| Loại State | Công cụ | Ví dụ |
|---|---|---|
| **Server State** — dữ liệu từ API | React Query | danh sách sản phẩm, đơn hàng, bookings |
| **Client State** — dữ liệu UI cục bộ | Zustand | thông tin đăng nhập, theme, sidebar open/close |
| **Form State** — dữ liệu form | React Hook Form | form tạo đơn hàng, form đăng ký |
| **URL State** — filter, pagination | React Router | `?page=0&status=PENDING&brandId=1` |

---

## 2. Zustand Stores

### 2.1 Auth Store

> Xem chi tiết trong `AUTH_FLOW.md`

**File:** `src/store/authStore.ts`

```typescript
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

type UserRole = 'ADMIN' | 'MANAGEMENT' | 'SALE' | 'TECHNICIAN' | 'CUSTOMER';

interface AuthState {
  accessToken: string | null;
  user: { id: number; username: string; roleName: UserRole } | null;
  expiresAt: number | null;

  setAuth: (token: string, expiresIn: number, user: AuthState['user']) => void;
  setAccessToken: (token: string) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
  hasRole: (...roles: UserRole[]) => boolean;
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
      name: 'angia-auth',
      // Chỉ persist user info (không persist accessToken — bảo mật)
      partialize: (state) => ({ user: state.user, expiresAt: state.expiresAt }),
    }
  )
);
```

### 2.2 UI Store (Optional)

**File:** `src/store/uiStore.ts`

```typescript
import { create } from 'zustand';

interface UIState {
  sidebarOpen: boolean;
  toggleSidebar: () => void;
  setSidebarOpen: (open: boolean) => void;
}

export const useUIStore = create<UIState>((set) => ({
  sidebarOpen: true,
  toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
  setSidebarOpen: (open) => set({ sidebarOpen: open }),
}));
```

---

## 3. React Query — Server State

### 3.1 Cấu Hình

**File:** `src/main.tsx`

```tsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,    // 5 phút — không re-fetch nếu data còn "tươi"
      gcTime: 10 * 60 * 1000,       // 10 phút — giữ cache sau khi component unmount
      retry: 1,                      // Thử lại 1 lần khi lỗi
      refetchOnWindowFocus: false,   // Không re-fetch khi focus window
    },
    mutations: {
      retry: 0,
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <QueryClientProvider client={queryClient}>
    <App />
    <ReactQueryDevtools initialIsOpen={false} />
  </QueryClientProvider>
);
```

### 3.2 Query Keys Convention

Dùng array-based keys có hierarchy để dễ invalidate:

```typescript
// src/api/queryKeys.ts
export const queryKeys = {
  // Products
  products: {
    all: ['products'] as const,
    list: (params: object) => ['products', 'list', params] as const,
    detail: (id: number) => ['products', 'detail', id] as const,
  },

  // Orders
  orders: {
    all: ['orders'] as const,
    list: (params: object) => ['orders', 'list', params] as const,
    detail: (id: number) => ['orders', 'detail', id] as const,
  },

  // Bookings
  bookings: {
    all: ['bookings'] as const,
    list: (params: object) => ['bookings', 'list', params] as const,
    detail: (id: number) => ['bookings', 'detail', id] as const,
  },

  // Customers
  customers: {
    all: ['customers'] as const,
    list: (params: object) => ['customers', 'list', params] as const,
    detail: (id: number) => ['customers', 'detail', id] as const,
    me: ['customers', 'me'] as const,
  },

  brands: {
    all: ['brands'] as const,
    list: (params: object) => ['brands', 'list', params] as const,
  },

  services: {
    all: ['services'] as const,
    list: (params: object) => ['services', 'list', params] as const,
  },

  users: {
    all: ['users'] as const,
    list: (params: object) => ['users', 'list', params] as const,
  },
};
```

### 3.3 Custom Hooks — Products

**File:** `src/features/products/useProductQueries.ts`

```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../../api/queryKeys';
import * as productApi from '../../api/productApi';
import toast from 'react-hot-toast';

// List với filter
export const useProducts = (params: {
  type?: 'MACHINE' | 'FILTER';
  brandId?: number;
  page?: number;
  size?: number;
}) => {
  return useQuery({
    queryKey: queryKeys.products.list(params),
    queryFn: () => productApi.getProducts(params),
  });
};

// Detail
export const useProduct = (id: number) => {
  return useQuery({
    queryKey: queryKeys.products.detail(id),
    queryFn: () => productApi.getProductById(id),
    enabled: !!id,
  });
};

// Create
export const useCreateProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: productApi.createProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.products.all });
      toast.success('Thêm sản phẩm thành công!');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Có lỗi xảy ra');
    },
  });
};

// Update
export const useUpdateProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: any }) =>
      productApi.updateProduct(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.products.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.products.detail(id) });
      toast.success('Cập nhật thành công!');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Có lỗi xảy ra');
    },
  });
};

// Soft delete
export const useDeleteProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: productApi.deleteProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.products.all });
      toast.success('Đã xóa sản phẩm!');
    },
  });
};
```

### 3.4 Custom Hooks — Orders

**File:** `src/features/orders/useOrderQueries.ts`

```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../../api/queryKeys';
import * as orderApi from '../../api/orderApi';
import toast from 'react-hot-toast';

export const useOrders = (params: {
  status?: string;
  customerId?: number;
  page?: number;
  size?: number;
}) => {
  return useQuery({
    queryKey: queryKeys.orders.list(params),
    queryFn: () => orderApi.getOrders(params),
  });
};

export const useOrder = (id: number) => {
  return useQuery({
    queryKey: queryKeys.orders.detail(id),
    queryFn: () => orderApi.getOrderById(id),
    enabled: !!id,
  });
};

export const useCreateOrder = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: orderApi.createOrder,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.orders.all });
      // Invalidate products cache (stock may change)
      queryClient.invalidateQueries({ queryKey: queryKeys.products.all });
      toast.success('Tạo đơn hàng thành công!');
    },
    onError: (error: any) => {
      const errorCode = error.response?.data?.errorCode;
      if (errorCode === 'INSUFFICIENT_STOCK') {
        toast.error('Không đủ tồn kho. Vui lòng kiểm tra lại!');
      } else {
        toast.error(error.response?.data?.message || 'Có lỗi xảy ra');
      }
    },
  });
};

export const useUpdateOrderStatus = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      orderApi.updateOrderStatus(id, status),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.orders.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.orders.detail(id) });
      // Invalidate products cache (stock may change on COMPLETED/CANCELLED)
      queryClient.invalidateQueries({ queryKey: queryKeys.products.all });
      toast.success('Cập nhật trạng thái thành công!');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Có lỗi xảy ra');
    },
  });
};
```

### 3.5 Custom Hooks — Bookings

**File:** `src/features/bookings/useBookingQueries.ts`

```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../../api/queryKeys';
import * as bookingApi from '../../api/bookingApi';
import toast from 'react-hot-toast';

export const useBookings = (params: {
  status?: string;
  customerId?: number;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}) => {
  return useQuery({
    queryKey: queryKeys.bookings.list(params),
    queryFn: () => bookingApi.getBookings(params),
  });
};

export const useAssignTechnician = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, technicianId }: { id: number; technicianId: number }) =>
      bookingApi.assignTechnician(id, technicianId),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.bookings.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.bookings.detail(id) });
      toast.success('Đã gán kỹ thuật viên!');
    },
  });
};

export const useCompleteBooking = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, notes }: { id: number; notes?: string }) =>
      bookingApi.completeBooking(id, notes),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.bookings.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.bookings.detail(id) });
      toast.success('Đã hoàn thành lịch bảo trì!');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Không thể hoàn thành lịch này');
    },
  });
};

export const useCancelBooking = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: bookingApi.cancelBooking,
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.bookings.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.bookings.detail(id) });
      toast.success('Đã hủy lịch bảo trì!');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Không thể hủy lịch này');
    },
  });
};
```

---

## 4. URL State — Filter & Pagination

Dùng URL params để lưu filter/pagination → user có thể share link, bookmark, back/forward hoạt động đúng.

```typescript
// Ví dụ: OrderListPage với URL state
import { useSearchParams } from 'react-router-dom';
import { useOrders } from './useOrderQueries';

export default function OrderListPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const status = searchParams.get('status') || undefined;
  const page = Number(searchParams.get('page') || '0');
  const size = Number(searchParams.get('size') || '10');

  const { data, isLoading } = useOrders({ status, page, size });

  const handleStatusFilter = (newStatus: string | null) => {
    setSearchParams((prev) => {
      if (newStatus) prev.set('status', newStatus);
      else prev.delete('status');
      prev.set('page', '0'); // Reset về trang đầu khi filter
      return prev;
    });
  };

  const handlePageChange = (newPage: number) => {
    setSearchParams((prev) => {
      prev.set('page', String(newPage));
      return prev;
    });
  };

  // URL ví dụ: /orders?status=PENDING&page=1&size=10
  // ...
}
```

---

## 5. Optimistic Updates

Dùng optimistic update khi UX quan trọng hơn consistency (ví dụ: lock/unlock user):

```typescript
export const useLockUser = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: userApi.lockUser,
    onMutate: async (userId: number) => {
      // Cancel outgoing queries
      await queryClient.cancelQueries({ queryKey: queryKeys.users.all });

      // Snapshot previous value
      const previousUsers = queryClient.getQueryData(queryKeys.users.list({}));

      // Optimistically update
      queryClient.setQueryData(queryKeys.users.list({}), (old: any) => ({
        ...old,
        content: old.content.map((u: any) =>
          u.id === userId ? { ...u, isActive: false } : u
        ),
      }));

      return { previousUsers };
    },
    onError: (_err, _userId, context) => {
      // Rollback on error
      queryClient.setQueryData(queryKeys.users.list({}), context?.previousUsers);
      toast.error('Không thể khóa tài khoản');
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.all });
    },
  });
};
```

---

## 6. Tóm Tắt Nguyên Tắc

| Nguyên tắc | Chi tiết |
|---|---|
| **Server state → React Query** | Không dùng useState/useEffect để fetch API |
| **Client state → Zustand** | Chỉ cho auth, UI toggles |
| **Form state → React Hook Form** | Không dùng Zustand/React Query cho form |
| **URL state → searchParams** | Filter, sort, pagination |
| **Invalidate sau mutation** | Luôn `invalidateQueries` sau create/update/delete |
| **Access token không vào localStorage** | Bảo mật chống XSS |
| **`withCredentials: true`** | Bắt buộc để cookie refreshToken hoạt động |
