# STITCH A.I — Prompt Tạo Giao Diện AnGia Management System

> **Hướng dẫn dùng:** Copy từng phần bên dưới vào STITCH A.I để tạo giao diện mẫu từng màn hình.

---

## 📋 Bối Cảnh Ứng Dụng

**Tên ứng dụng:** AnGia Management System  
**Loại:** Web App quản lý bán hàng & bảo trì máy lọc nước  
**Stack Frontend:** React + TypeScript + Tailwind CSS  
**Ngôn ngữ giao diện:** Tiếng Việt  

**Màu sắc chủ đạo:**
- Primary: Xanh dương đậm `#1e40af` (blue-800)
- Accent: Xanh dương sáng `#3b82f6` (blue-500)
- Background: Xám nhạt `#f8fafc`
- Sidebar: Trắng hoặc xanh navy `#1e293b`
- Text chính: `#0f172a`
- Text phụ: `#64748b`

**Vai trò người dùng:**
- ADMIN — Toàn quyền
- MANAGEMENT — Quản lý sản phẩm, dịch vụ, khách hàng, đơn hàng, lịch bảo trì
- SALE — Xem khách hàng của mình, tạo đơn hàng
- TECHNICIAN — Xem và hoàn thành lịch bảo trì được gán
- CUSTOMER — Xem hồ sơ và đặt lịch bảo trì của mình

---

## 🎨 Prompt 1 — Tổng Quan Layout

```
Design a Vietnamese enterprise web application dashboard for "AnGia Management System" — a water filter machine sales and maintenance management platform.

Layout:
- Left sidebar: 240px wide, white background, shadow-sm, with the logo "AnGia" at the top (blue water drop icon + bold text), navigation menu items below
- Top header: full-width, white, 64px height, shows current page title on the left, user avatar + name + role badge + logout button on the right
- Main content area: gray-50 background, padded 24px, scrollable

Sidebar navigation menu items (icon + label):
1. 📊 Tổng quan (Dashboard) — visible to all roles
2. 👥 Quản lý người dùng — ADMIN only, shown with lock icon if hidden
3. 🤝 Khách hàng — ADMIN, MANAGEMENT, SALE
4. 👤 Hồ sơ của tôi — CUSTOMER only
5. 📦 Sản phẩm — all roles
6. 🏷️ Thương hiệu — ADMIN, MANAGEMENT
7. 🔧 Dịch vụ — all roles
8. 🛒 Đơn hàng — all roles
9. 📅 Lịch bảo trì — all roles

Active menu item: blue-600 background, white text, rounded-lg
Inactive: gray-700 text, hover blue-50 background

Show the layout in desktop (1280px wide) with "Tổng quan" as the active menu item.
```

---

## 🎨 Prompt 2 — Trang Đăng Nhập (Login Page)

```
Design a Vietnamese login page for "AnGia Management System" — a water filter sales and maintenance platform.

Layout: centered card on a blue gradient background (from blue-700 to blue-900)

Card (white, rounded-2xl, shadow-xl, 400px wide, padded 40px):
- Top: Blue water drop logo icon (48px) centered, bold title "AnGia" below it, subtitle "Hệ thống quản lý An Gia" in gray-500
- Form fields:
  - Label "Tên đăng nhập", text input with user icon, placeholder "Nhập tên đăng nhập"
  - Label "Mật khẩu", password input with lock icon + show/hide toggle, placeholder "Nhập mật khẩu"
- Primary button full-width: "Đăng nhập" in blue-600, rounded-lg
- Below button: link "Chưa có tài khoản? Đăng ký ngay" in blue-500
- Error state: red banner at top of card with icon "Tên đăng nhập hoặc mật khẩu không đúng"

Show desktop layout with an error state visible.
```

---

## 🎨 Prompt 3 — Trang Đăng Ký (Register Page)

```
Design a Vietnamese registration page for "AnGia Management System".

Layout: centered card on blue gradient background

Card (white, rounded-2xl, shadow-xl, 480px wide, padded 40px):
- Header: logo + title "Tạo tài khoản mới" + subtitle "Đăng ký để sử dụng dịch vụ An Gia"
- Form fields in single column:
  - "Tên đăng nhập" — text input, helper "3-100 ký tự"
  - "Mật khẩu" — password input with toggle, helper "Tối thiểu 6 ký tự"
  - "Họ và tên" — text input
  - "Số điện thoại" — text input with phone icon, placeholder "0901234567"
  - "Địa chỉ" — textarea 2 rows, optional badge shown
- Submit button: "Đăng ký" blue-600 full-width
- Link: "Đã có tài khoản? Đăng nhập"
- Validation error example: red border + error text "Số điện thoại không hợp lệ" under phone field

Show desktop layout with a validation error on the phone field.
```

---

## 🎨 Prompt 4 — Dashboard / Tổng Quan

```
Design a Vietnamese dashboard page for "AnGia Management System" shown inside the main app layout (sidebar + header).

Page title: "Tổng quan"

Content layout:
1. Stats row (4 cards in a grid):
   - Card 1: "Tổng đơn hàng hôm nay" — big number "12", icon shopping cart, blue-500 accent, small trend "+3 so với hôm qua" in green
   - Card 2: "Lịch bảo trì hôm nay" — number "5", icon calendar, purple-500 accent
   - Card 3: "Đơn hàng chờ xử lý" — number "8", icon clock, yellow-500 accent
   - Card 4: "Doanh thu tháng này" — "42.500.000 đ", icon currency, green-500 accent
   Each card: white, rounded-xl, shadow-sm, padded 20px, icon in colored circle on right

2. Two-column row below:
   - Left (60%): "Đơn hàng gần đây" table card
     - Columns: Mã đơn | Khách hàng | Trạng thái | Tổng tiền | Ngày tạo
     - 5 sample rows with status badges (PENDING=yellow, PROCESSING=blue, COMPLETED=green, CANCELLED=red)
     - "Xem tất cả" link at bottom
   - Right (40%): "Lịch bảo trì hôm nay" list card
     - Each item: customer name, service name, time, technician assigned (or "Chưa gán" in orange), status badge

Show desktop layout (1280px).
```

---

## 🎨 Prompt 5 — Danh Sách Sản Phẩm (Product List)

```
Design a Vietnamese product listing page for a water filter management system inside the app layout.

Page title: "Sản phẩm"

Top toolbar (space-between):
- Left: Search input with magnifier icon, placeholder "Tìm kiếm sản phẩm..."
- Center: Filter tabs "Tất cả | Máy lọc (MACHINE) | Lõi lọc (FILTER)"
- Right: Dropdown "Thương hiệu" filter + button "Thêm sản phẩm" in blue-600 (shown only for ADMIN/MANAGEMENT role)

Product grid (3 columns, gap-6):
Each product card (white, rounded-xl, shadow-sm):
- Top: product image placeholder (gray-100 background, 200px height, "📦" icon centered) with a badge top-right showing product type (MACHINE=blue, FILTER=green)
- Body:
  - Brand tag (small, gray)
  - Product name bold (e.g., "Máy lọc nước Aqua A5")
  - Product code (AQ-001) in gray-500, small
  - Price: "5.500.000 đ" in blue-600 bold large
  - Stock: "Tồn kho: 15" with green dot if >0, red dot if 0
  - Warranty: "Bảo hành: 24 tháng"
- Footer: two buttons "Xem chi tiết" (outline) + "Chỉnh sửa" (blue filled) side by side

Pagination at bottom: Previous | 1 2 3 ... | Next

Show 6 products in the grid, desktop layout.
```

---

## 🎨 Prompt 6 — Chi Tiết Sản Phẩm (Product Detail)

```
Design a Vietnamese product detail page for a water filter management system inside the app layout.

Page title: "Chi tiết sản phẩm" with back arrow button

Two-column layout:
Left (40%): 
- Large image placeholder (white card, rounded-xl, 400px height)
- Thumbnail row below (3 small thumbnails)
- Type badge: "MÁY LỌC NƯỚC" in blue

Right (60%):
- Brand: "Aqua" with small logo icon
- Product name h1: "Máy lọc nước Aqua A5"
- Product code: "AQ-001" in gray tag
- Price: "5.500.000 đ" in blue-600, very large, bold
- Specs grid (2 columns):
  - Model: "A5-2024"
  - Tồn kho: "15" with green stock badge
  - Bảo hành: "24 tháng"
  - Tuổi thọ: "60 tháng"
  - Công suất: "10L/hour"
  - Tầng lọc: "9 tầng"
- Description section: "Mô tả sản phẩm" heading + text paragraph
- Action buttons (ADMIN/MANAGEMENT only): "Chỉnh sửa" blue + "Xóa sản phẩm" red outline

Show desktop layout.
```

---

## 🎨 Prompt 7 — Danh Sách Đơn Hàng (Order List)

```
Design a Vietnamese order management page inside the app layout.

Page title: "Đơn hàng"

Top toolbar:
- Left: Search by order code, placeholder "Tìm mã đơn hàng..."
- Filter row: Status tabs "Tất cả | PENDING | PROCESSING | COMPLETED | CANCELLED" as pills/badges
- Right: button "Tạo đơn hàng" in blue-600

Data table (white card, rounded-xl, shadow-sm):
Columns: Mã đơn | Khách hàng | Nhân viên bán | Trạng thái | Tổng tiền | Ngày tạo | Hành động

Sample rows:
1. ORD-20250115-12345 | Nguyễn Văn A | sale01 | [PENDING yellow badge] | 11.000.000 đ | 15/01/2025 | [Xem] button
2. ORD-20250114-54321 | Trần Thị B | sale02 | [PROCESSING blue badge] | 5.500.000 đ | 14/01/2025 | [Xem] button
3. ORD-20250113-99999 | Lê Văn C | sale01 | [COMPLETED green badge] | 3.200.000 đ | 13/01/2025 | [Xem] button
4. ORD-20250112-11111 | Phạm Thị D | sale03 | [CANCELLED red badge] | 8.000.000 đ | 12/01/2025 | [Xem] button

Status badge colors: PENDING=yellow-100/yellow-700, PROCESSING=blue-100/blue-700, COMPLETED=green-100/green-700, CANCELLED=red-100/red-700

Pagination: showing "Hiển thị 1-10 trong 42 kết quả" + Previous/Next buttons

Show desktop layout (1280px).
```

---

## 🎨 Prompt 8 — Chi Tiết Đơn Hàng (Order Detail)

```
Design a Vietnamese order detail page inside the app layout.

Page title: "Chi tiết đơn hàng" with back arrow

Two sections:

Top info card (white, rounded-xl, padded 24px):
- Header row: order code "ORD-20250115-12345" in large bold + status badge "ĐANG XỬ LÝ" blue pill (right aligned)
- Grid 2 columns:
  - Left: Thông tin khách hàng — name "Nguyễn Văn A", phone "0901234567", address "123 Đường ABC, TP.HCM"
  - Right: Thông tin đơn hàng — Ngày tạo "15/01/2025 10:00", Nhân viên bán "sale01", Địa chỉ giao hàng "123 Đường ABC"
- Status update section (ADMIN/MANAGEMENT only): dropdown to change status + "Cập nhật" button

Bottom products table card (white, rounded-xl):
- Title "Sản phẩm trong đơn hàng"
- Table columns: STT | Sản phẩm | Mã SP | Đơn giá | Số lượng | Thành tiền
- 2 sample product rows with product names, codes, prices
- Footer row: "Tổng cộng" right-aligned: "11.000.000 đ" in blue-600 bold

Show desktop layout.
```

---

## 🎨 Prompt 9 — Tạo Đơn Hàng (Order Create)

```
Design a Vietnamese order creation page inside the app layout.

Page title: "Tạo đơn hàng mới" with back arrow

Two-column layout:
Left column (60%):
- Card "Thông tin đơn hàng":
  - Customer search field: "Chọn khách hàng" with search icon, opens dropdown showing customer list
  - Shipping address textarea with placeholder "Địa chỉ giao hàng..."
- Card "Sản phẩm":
  - Product search input "Tìm sản phẩm để thêm..." + "Thêm" button
  - Selected products table: Product name | Đơn giá | Số lượng (number input, min 1) | Thành tiền | Xóa (trash icon)
  - 2 sample products in the table
  - "Thêm sản phẩm khác" dashed button at bottom

Right column (40%):
- Order summary card (white, rounded-xl, sticky):
  - Title "Tóm tắt đơn hàng"
  - List of selected items: name + qty + subtotal
  - Divider
  - "Tổng cộng: 11.000.000 đ" in blue-600 bold large
  - "Tạo đơn hàng" blue-600 full-width button
  - Stock warning alert (yellow): "Máy lọc Aqua A5: chỉ còn 3 trong kho"

Show desktop layout.
```

---

## 🎨 Prompt 10 — Danh Sách Lịch Bảo Trì (Booking List)

```
Design a Vietnamese maintenance booking management page inside the app layout.

Page title: "Lịch bảo trì"

Top toolbar:
- Search: "Tìm mã lịch hoặc tên khách..."
- Date range filter: "Từ ngày" and "Đến ngày" date pickers
- Status filter tabs: "Tất cả | PENDING | CONFIRMED | COMPLETED | CANCELLED"
- Button "Đặt lịch mới" blue-600

Data table (white card, rounded-xl):
Columns: Mã lịch | Khách hàng | Dịch vụ | Ngày hẹn | Kỹ thuật viên | Trạng thái | Hành động

Sample rows:
1. BK-20250120-54321 | Nguyễn Văn A | Vệ sinh bộ lọc | 20/01/2025 09:00 | [Chưa gán - orange badge] | [PENDING - yellow] | [Xem][Gán KTV]
2. BK-20250121-11111 | Trần Thị B | Thay màng RO | 21/01/2025 14:00 | tech01 | [CONFIRMED - blue] | [Xem][Hoàn thành][Hủy]
3. BK-20250115-77777 | Lê Văn C | Kiểm tra định kỳ | 15/01/2025 10:00 | tech02 | [COMPLETED - green] | [Xem]

Action buttons adapt by status:
- PENDING: Xem + Gán KTV button (blue outline) + Hủy (red outline)
- CONFIRMED: Xem + Hoàn thành (green) + Hủy (red outline)
- COMPLETED/CANCELLED: Xem only

Show desktop layout.
```

---

## 🎨 Prompt 11 — Chi Tiết Lịch Bảo Trì (Booking Detail)

```
Design a Vietnamese maintenance booking detail page inside the app layout.

Page title: "Chi tiết lịch bảo trì" with back arrow

Two-column info card (white, rounded-xl, padded 24px):
Header: booking code "BK-20250120-54321" large bold + status "CHỜ XÁC NHẬN" yellow pill

Left column: Thông tin khách hàng
- Tên: Nguyễn Văn A
- SĐT: 0901234567
- Địa chỉ: 123 Đường ABC, TP.HCM

Right column: Thông tin lịch
- Dịch vụ: Vệ sinh bộ lọc — with wrench icon
- Giá dịch vụ: 150.000 đ
- Ngày hẹn: 20/01/2025 lúc 09:00
- Ngày tạo: 15/01/2025 10:00
- Ghi chú: "Máy bị rò rỉ ở ống nước" (italic, gray)
- Kỹ thuật viên: "Chưa được gán" in orange-500

Action section (ADMIN/MANAGEMENT): 
- "Gán kỹ thuật viên" section: dropdown to select technician + "Gán" button blue
- "Hủy lịch" red outline button

Show desktop layout.
```

---

## 🎨 Prompt 12 — Danh Sách Khách Hàng (Customer List)

```
Design a Vietnamese customer management page inside the app layout.

Page title: "Khách hàng"

Top toolbar:
- Search input "Tìm tên hoặc số điện thoại..."
- Button "Thêm khách hàng" blue-600

Data table (white card, rounded-xl, shadow-sm):
Columns: STT | Họ và tên | Số điện thoại | Địa chỉ | Nhân viên tạo | Ngày tạo | Hành động

Sample 5 rows with Vietnamese names, phone numbers, addresses
Action column: [Xem] icon button + [Sửa] icon button

Hoverable rows, alternating subtle background

Pagination below: "Hiển thị 1-10 trong 87 khách hàng"

Show desktop layout.
```

---

## 🎨 Prompt 13 — Quản Lý Người Dùng (User List — ADMIN)

```
Design a Vietnamese user management page inside the app layout (visible only to ADMIN role).

Page title: "Quản lý người dùng"

Top toolbar:
- Search: "Tìm tên đăng nhập..."
- Filter: Role dropdown "Tất cả vai trò | ADMIN | MANAGEMENT | SALE | TECHNICIAN"
- Button "Tạo tài khoản" blue-600

Data table (white card, rounded-xl):
Columns: ID | Tên đăng nhập | Vai trò | Trạng thái | Ngày tạo | Hành động

Sample rows:
1. 1 | admin | [ADMIN - purple badge] | [Hoạt động - green badge] | 01/01/2025 | [Sửa]
2. 2 | manager01 | [MANAGEMENT - blue badge] | [Hoạt động - green] | 01/01/2025 | [Sửa][Khóa]
3. 3 | sale01 | [SALE - teal badge] | [Hoạt động - green] | 01/01/2025 | [Sửa][Khóa]
4. 4 | tech01 | [TECHNICIAN - orange badge] | [Bị khóa - red badge] | 01/01/2025 | [Sửa][Mở khóa]

Role badge colors: ADMIN=purple, MANAGEMENT=blue, SALE=teal, TECHNICIAN=orange, CUSTOMER=gray

"Khóa" action shows a confirmation tooltip or inline confirm

Show desktop layout.
```

---

## 🎨 Prompt 14 — Modal Tạo/Sửa (Create/Edit Modal)

```
Design a Vietnamese modal dialog for creating or editing a product in a water filter management system.

Modal overlay: dark semi-transparent backdrop
Modal card: white, rounded-2xl, shadow-2xl, 560px wide, max-height 80vh with scroll

Header: "Thêm sản phẩm mới" bold h2 + X close button right

Form body (scrollable):
- Row 1 (2 cols): "Mã sản phẩm" text input required | "Loại sản phẩm" select (Máy lọc / Lõi lọc)
- Row 2 (2 cols): "Thương hiệu" select dropdown | "Model" text input
- "Tên sản phẩm" full-width text input required
- Row 3 (2 cols): "Giá bán (đ)" number input | "Tồn kho" number input
- Row 4 (2 cols): "Bảo hành (tháng)" number input | "Tuổi thọ (tháng)" number input
- "Mô tả" textarea 3 rows
- "Thông số kỹ thuật (JSON)" textarea 2 rows with placeholder '{"capacity": "10L/hour", "stages": 9}'

Footer (sticky): "Hủy" outline button + "Lưu sản phẩm" blue-600 button

Validation: show red borders + inline error messages on required fields
Show the modal open state with one validation error on the name field.
```

---

## 🎨 Prompt 15 — Hồ Sơ Khách Hàng (My Profile — CUSTOMER role)

```
Design a Vietnamese "My Profile" page inside the app layout for a customer user.

Page title: "Hồ sơ của tôi"

Centered content (max-width 640px, auto margin):

Profile card (white, rounded-2xl, shadow-sm, padded 32px):
- Large avatar circle (80px): initials "NA" on blue-600 background, centered
- Name "Nguyễn Văn A" bold h2 centered
- Role badge "KHÁCH HÀNG" gray centered
- Member since: "Thành viên từ 15/01/2025" small gray centered

Info section below (labeled fields, read-only style):
- SĐT: 0901234567 — with edit pencil icon on right
- Địa chỉ: 123 Đường ABC, TP.HCM — with edit pencil icon
- Tên đăng nhập: john_doe (no edit)

Edit form (shown inline when pencil clicked):
- Editable fields for Họ và tên, SĐT, Địa chỉ
- "Lưu thay đổi" blue button + "Hủy" outline button

Activity summary section:
- Two mini stat cards side by side: "3 đơn hàng" and "2 lịch bảo trì"

Show desktop layout in read mode (no edit open).
```

---

## 🎨 Prompt 16 — Danh Sách Thương Hiệu & Dịch Vụ (Brand & Service List)

```
Design a Vietnamese brands and services management page inside the app layout. Show as two separate sections stacked vertically.

Page title: "Thương hiệu & Dịch vụ"

Section 1: "Thương hiệu"
- Header row: "Thương hiệu" h2 + "Thêm thương hiệu" button blue-600 right
- Grid 4 columns of brand cards (white, rounded-xl, shadow-sm, padded 16px):
  - Logo placeholder (60px circle, brand initial letter, colored)
  - Brand name bold (Aqua, Kangaroo, Karofi, Sunhouse)
  - Short description in gray-500, 2 lines max
  - Footer: [Sửa] + [Xóa] icon buttons

Section 2: "Dịch vụ bảo trì"
- Header row: "Dịch vụ" h2 + "Thêm dịch vụ" button blue-600 right
- Data table (white card, rounded-xl):
  Columns: Tên dịch vụ | Giá cơ bản | Thời gian (phút) | Hành động
  Rows:
  1. Vệ sinh bộ lọc | 150.000 đ | 60 phút | [Sửa][Xóa]
  2. Sửa chữa máy lọc nước | 300.000 đ | 90 phút | [Sửa][Xóa]
  3. Lắp đặt máy mới | 200.000 đ | 120 phút | [Sửa][Xóa]
  4. Thay màng RO | 400.000 đ | 60 phút | [Sửa][Xóa]
  5. Kiểm tra định kỳ | 80.000 đ | 30 phút | [Sửa][Xóa]

Show desktop layout (1280px).
```

---

## 🎨 Prompt 17 — Mobile Responsive Layout

```
Design the mobile responsive version (375px width) of the AnGia Management System.

Show 3 mobile screens side by side:

Screen 1 — Mobile Sidebar (drawer open):
- Hamburger menu at top-left of header
- Full-height drawer from left: logo at top, nav menu items stacked vertically with icons
- Dark overlay behind drawer

Screen 2 — Mobile Order List:
- Header with title "Đơn hàng" + hamburger icon
- Status filter pills (horizontal scroll): Tất cả | PENDING | PROCESSING | COMPLETED
- Card-based list (NOT table): each order is a card with order code, customer name, status badge, total amount, date
- Floating "+" button bottom-right corner for creating new order

Screen 3 — Mobile Dashboard Stats:
- 2x2 grid of stat cards (stacked in 2 columns)
- Recent orders as cards below (vertical list)
- Bottom navigation bar: Dashboard | Đơn hàng | Lịch BT | Tài khoản icons

Use clean mobile-first design with 16px base padding, touch-friendly 44px+ tap targets.
```

---

## 🎨 Prompt 18 — Empty States & Loading States

```
Design empty states and loading states for the AnGia Management System.

Show 4 states side by side in desktop app layout:

State 1 — Empty Order List:
- Center of table area: shopping cart outline icon (64px, gray-300)
- "Chưa có đơn hàng nào" h3 gray-500
- "Tạo đơn hàng đầu tiên" blue-600 button below

State 2 — Table Loading Skeleton:
- 5 rows of skeleton bars animating (pulse animation)
- Each row: 5 gray rounded placeholder bars of varying widths

State 3 — Search No Results:
- Magnifier icon with X (64px, gray-300)
- "Không tìm thấy kết quả" h3 gray-500
- "Thử từ khóa khác hoặc xóa bộ lọc" in gray-400

State 4 — Page Loading Spinner:
- Full page overlay with white background
- Centered: AnGia logo + spinning blue circle
- "Đang tải..." text below

Use soft gray colors for all skeleton/empty states, animate the skeleton with shimmer effect.
```

---

## 📌 Ghi Chú Cho STITCH A.I

- **Ngôn ngữ:** Tất cả nhãn, placeholder, nội dung mẫu đều bằng tiếng Việt
- **Design system:** Tailwind CSS utility classes — rounded-xl, shadow-sm, gap-6, padded 24px
- **Breakpoints:** Desktop first (1280px), mobile (375px)
- **Currency format:** số + " đ" (VD: 5.500.000 đ)
- **Date format:** dd/mm/yyyy HH:MM (VD: 15/01/2025 10:00)
- **Status badges:** Rounded-full pills, colored background + colored text (không dùng solid dark backgrounds)
- **Typography:** Font sans-serif (Inter hoặc tương đương), heading bold, body regular
- **Icons:** Heroicons style (outline), consistent 20-24px size
