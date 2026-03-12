-- ============================================================
-- SAMPLE DATA FOR TESTING
-- Database: angia_management
-- Created: 2026-03-13
-- ===========================================================
-- 3. BRANDS (Hãng sản xuất)
-- ============================================================
INSERT INTO brands (name, description, logo_url, is_deleted, created_at, updated_at) VALUES
('KAROFI', 'Máy lọc nước cao cấp từ Hàn Quốc', 'https://example.com/karofi.png', FALSE, NOW(), NOW()),
('KANGAROO', 'Máy lọc nước tổng đại lý từ Việt Nam', 'https://example.com/kangaroo.png', FALSE, NOW(), NOW()),
('ELTECH', 'Hệ thống lọc nước thông minh', 'https://example.com/eltech.png', FALSE, NOW(), NOW()),
('SUNHOUSE', 'Thiết bị gia dụng uy tín của Việt Nam', 'https://example.com/sunhouse.png', FALSE, NOW(), NOW()),
('RO-MASTER', 'Công nghệ RO hiện đại nhất', 'https://example.com/ro-master.png', FALSE, NOW(), NOW()),
('AQUA PURE', 'Máy lọc nước tiên tiến từ Nhật Bản', 'https://example.com/aquapure.png', FALSE, NOW(), NOW()),
('ECOSMART', 'Giải pháp nước sạch bền vững', 'https://example.com/ecosmart.png', FALSE, NOW(), NOW());

-- ============================================================
-- 4. PRODUCTS (Sản phẩm - Máy lọc + Lõi lọc)
-- ============================================================
INSERT INTO products (product_code, product_type, brand_id, name, model, description, price, stock_quantity, warranty_months, lifespan_months, specs_json, is_deleted, created_at, updated_at) VALUES
-- MACHINES
('KAROFI-PRO-001', 'MACHINE', 1, 'Máy Lọc Nước KAROFI Pro', 'KAR-P1000', 'Máy lọc nước RO 5 cấp lọc, công suất 40L/h', 6500000, 5, 24, NULL, '{"flow_rate":"40L/h","pressure":"100psi","origin":"Korea","filtration":"5-stage"}', FALSE, NOW(), NOW()),
('KANGAROO-ECO-002', 'MACHINE', 2, 'Máy Lọc Nước KANGAROO Eco', 'KGO-E800', 'Máy lọc nước công nghệ Nano, tiết kiệm điện', 4200000, 8, 18, NULL, '{"flow_rate":"50L/h","pressure":"80psi","origin":"Vietnam","filtration":"nano"}', FALSE, NOW(), NOW()),
('ELTECH-SMART-003', 'MACHINE', 3, 'Máy Lọc Thông Minh ELTECH', 'ELT-S600', 'Máy lọc có màn hình cảm ứng, wifi kết nối', 7800000, 3, 36, NULL, '{"flow_rate":"60L/h","pressure":"120psi","origin":"Korea","features":"smart,wifi"}', FALSE, NOW(), NOW()),
('SUNHOUSE-001', 'MACHINE', 4, 'Máy Lọc SUNHOUSE Basic', 'SUN-B400', 'Máy lọc cơ bản, giá phổ thông', 2500000, 10, 12, NULL, '{"flow_rate":"30L/h","pressure":"70psi","origin":"Vietnam","filtration":"basic"}', FALSE, NOW(), NOW()),
('ROMASTER-PRO-004', 'MACHINE', 5, 'Máy RO MASTER Professional', 'ROM-PRO500', 'Máy RO công suất cao, dùng cho gia đình lớn', 8500000, 2, 24, NULL, '{"flow_rate":"75L/h","pressure":"150psi","origin":"Taiwan","filtration":"RO"}', FALSE, NOW(), NOW()),
('AQUAPURE-001', 'MACHINE', 6, 'Máy Lọc AQUA PURE', 'AQP-A250', 'Hệ thống lọc nước Nhật Bản', 5500000, 6, 30, NULL, '{"flow_rate":"35L/h","pressure":"90psi","origin":"Japan","filtration":"multi-stage"}', FALSE, NOW(), NOW()),
('ECOSMART-ECO-005', 'MACHINE', 7, 'Máy Lọc ECOSMART Eco', 'ECO-S300', 'Giải pháp lọc nước thân thiện môi trường', 3800000, 7, 18, NULL, '{"flow_rate":"45L/h","pressure":"85psi","origin":"Vietnam","features":"eco-friendly"}', FALSE, NOW(), NOW()),

-- FILTERS
('KAROFI-FILTER-001', 'FILTER', 1, 'Lõi Lọc KAROFI PP 10mic', 'KAR-PP10', 'Lõi lọc PP cho giai đoạn 1', 150000, 50, NULL, 3, '{"type":"PP","micron":"10","material":"polypropylene"}', FALSE, NOW(), NOW()),
('KAROFI-FILTER-002', 'FILTER', 1, 'Lõi Lọc KAROFI Carbon', 'KAR-CB05', 'Lõi lọc than hoạt tính cho giai đoạn 2', 250000, 40, NULL, 6, '{"type":"Carbon","micron":"5","material":"activated_carbon"}', FALSE, NOW(), NOW()),
('KANGAROO-FILTER-001', 'FILTER', 2, 'Lõi Lọc KANGAROO PP', 'KGO-PP10', 'Lõi lọc PP chất lượng cao', 120000, 60, NULL, 3, '{"type":"PP","micron":"10","material":"polypropylene"}', FALSE, NOW(), NOW()),
('RO-FILTER-MEMBRANE', 'FILTER', 5, 'Màng Lọc RO MASTER', 'ROM-MB50', 'Màng lọc RO 50 GPD', 800000, 15, NULL, 12, '{"type":"Membrane","capacity":"50GPD","material":"RO"}', FALSE, NOW(), NOW()),
('UNIVERSAL-FILTER-001', 'FILTER', 6, 'Lõi Lọc Universal', 'UNI-PPM', 'Lõi lọc PP phổ dụng cho nhiều dòng máy', 100000, 80, NULL, 3, '{"type":"PP","micron":"10","material":"polypropylene"}', FALSE, NOW(), NOW()),
('CARTRIDGE-CERAMIC', 'FILTER', 7, 'Lõi Lọc Gốm ECOSMART', 'ECO-CRM', 'Lõi lọc gốm nano chất lượng cao', 180000, 45, NULL, 4, '{"type":"Ceramic","micron":"0.2","material":"nano_ceramic"}', FALSE, NOW(), NOW()),
('FILTER-KDF-MEDIA', 'FILTER', 5, 'Vật Liệu Lọc KDF MASTER', 'ROM-KDF', 'Vật liệu lọc KDF kháng khuẩn', 320000, 30, NULL, 8, '{"type":"KDF","material":"KDF-55"}', FALSE, NOW(), NOW());

-- ============================================================
-- 5. PRODUCT IMAGES (Hình ảnh sản phẩm)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_main) VALUES
(1, 'https://example.com/karofi-pro-001.jpg', TRUE),
(1, 'https://example.com/karofi-pro-002.jpg', FALSE),
(2, 'https://example.com/kangaroo-eco-001.jpg', TRUE),
(2, 'https://example.com/kangaroo-eco-002.jpg', FALSE),
(3, 'https://example.com/eltech-smart-001.jpg', TRUE),
(4, 'https://example.com/sunhouse-001.jpg', TRUE),
(5, 'https://example.com/romaster-pro-001.jpg', TRUE),
(6, 'https://example.com/aquapure-001.jpg', TRUE),
(7, 'https://example.com/ecosmart-eco-001.jpg', TRUE);

-- ============================================================
-- 6. CUSTOMERS (Hồ sơ khách hàng - CRM)
-- ============================================================
INSERT INTO customers (full_name, phone, address, created_by, created_at, updated_at) VALUES
('Nguyễn Văn A', '0901234567', '123 Đường Lê Lợi, TP.HCM', 3, NOW(), NOW()),
('Trần Thị B', '0912345678', '456 Đường Nguyễn Huệ, TP.HCM', 3, NOW(), NOW()),
('Hoàng Văn C', '0923456789', '789 Đường Tôn Đức Thắng, Hà Nội', 3, NOW(), NOW()),
('Phạm Thị D', '0934567890', '321 Đường Cách Mạng Tháng 8, TP.HCM', 4, NOW(), NOW()),
('Lê Văn E', '0945678901', '654 Đường Phạm Ngũ Lão, TP.HCM', 4, NOW(), NOW()),
('Vũ Thị F', '0956789012', '987 Đường Hai Bà Trưng, Hà Nội', 3, NOW(), NOW()),
('Đặng Văn G', '0967890123', '159 Đường Lý Thường Kiệt, Đà Nẵng', 4, NOW(), NOW());

-- ============================================================
-- 7. ORDERS (Đơn hàng)
-- ============================================================
INSERT INTO orders (order_code, customer_id, sale_id, total_amount, status, shipping_address, created_at, updated_at) VALUES
('ORD-2026-001', 1, 3, 6650000, 'PENDING', '123 Đường Lê Lợi, TP.HCM', NOW(), NOW()),
('ORD-2026-002', 2, 3, 4420000, 'PROCESSING', '456 Đường Nguyễn Huệ, TP.HCM', NOW(), NOW()),
('ORD-2026-003', 3, 4, 16300000, 'COMPLETED', '789 Đường Tôn Đức Thắng, Hà Nội', NOW(), NOW()),
('ORD-2026-004', 4, 4, 5500000, 'PENDING', '321 Đường Cách Mạng Tháng 8, TP.HCM', NOW(), NOW()),
('ORD-2026-005', 5, 3, 8500000, 'PROCESSING', '654 Đường Phạm Ngũ Lão, TP.HCM', NOW(), NOW()),
('ORD-2026-006', 6, 4, 3800000, 'CANCELLED', '987 Đường Hai Bà Trưng, Hà Nội', NOW(), NOW()),
('ORD-2026-007', 7, 3, 14250000, 'PENDING', '159 Đường Lý Thường Kiệt, Đà Nẵng', NOW(), NOW());

-- ============================================================
-- 8. ORDER ITEMS (Chi tiết đơn hàng)
-- ============================================================
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
-- ORD-2026-001: KAROFI Pro + Filter
(1, 1, 1, 6500000),
(1, 9, 1, 150000),

-- ORD-2026-002: KANGAROO Eco + Filter
(2, 2, 1, 4200000),
(2, 11, 1, 220000),

-- ORD-2026-003: ELTECH Smart + RO Master + Multiple Filters
(3, 3, 1, 7800000),
(3, 5, 1, 8500000),
(3, 12, 1, 0),

-- ORD-2026-004: AQUA PURE + Filter
(4, 6, 1, 5500000),
(4, 13, 1, 0),

-- ORD-2026-005: RO Master only
(5, 5, 1, 8500000),
(5, 14, 1, 0),

-- ORD-2026-006: ECOSMART Eco (CANCELLED)
(6, 7, 1, 3800000),
(6, 15, 1, 0),

-- ORD-2026-007: Multiple machines
(7, 1, 1, 6500000),
(7, 4, 1, 2500000),
(7, 2, 1, 5250000);

-- ============================================================
-- 9. SERVICES (Danh mục dịch vụ)
-- ============================================================
INSERT INTO services (service_code, name, description, base_price, duration_minutes, is_deleted, created_at) VALUES
('SRV-001', 'Vệ sinh máy lọc nước', 'Vệ sinh toàn bộ bộ phận máy, kiểm tra hệ thống', 250000, 60, FALSE, NOW()),
('SRV-002', 'Thay lõi lọc PP giai đoạn 1', 'Thay lõi lọc PP 10 micron', 150000, 30, FALSE, NOW()),
('SRV-003', 'Thay lõi lọc Carbon giai đoạn 2', 'Thay lõi lọc than hoạt tính', 200000, 30, FALSE, NOW()),
('SRV-004', 'Thay màng lọc RO', 'Thay thế màng lọc RO 50 GPD', 500000, 45, FALSE, NOW()),
('SRV-005', 'Khám máy định kỳ 6 tháng', 'Kiểm tra toàn diện hệ thống, test chất lượng nước', 300000, 90, FALSE, NOW()),
('SRV-006', 'Sửa chữa - Thay thế linh kiện', 'Sửa chữa máy, thay bơm, van, cảm biến...', 350000, 120, FALSE, NOW()),
('SRV-007', 'Bảo dưỡng hệ thống RO', 'Bảo dưỡng toàn hệ thống RO, kiểm tra áp suất', 400000, 90, FALSE, NOW());

-- ============================================================
-- 10. MAINTENANCE BOOKINGS (Đặt lịch bảo trì)
-- ============================================================
INSERT INTO maintenance_bookings (booking_code, customer_id, service_id, technician_id, booking_date, status, notes, created_at) VALUES
('BOOK-2026-001', 1, 1, 5, DATE_ADD(NOW(), INTERVAL 2 DAY), 'PENDING', NULL, NOW()),
('BOOK-2026-002', 2, 2, 6, DATE_ADD(NOW(), INTERVAL 3 DAY), 'CONFIRMED', NULL, NOW()),
('BOOK-2026-003', 3, 5, 5, DATE_ADD(NOW(), INTERVAL 5 DAY), 'PENDING', NULL, NOW()),
('BOOK-2026-004', 4, 3, 7, DATE_ADD(NOW(), INTERVAL 1 DAY), 'COMPLETED', 'Đã thay lõi lọc carbon, máy hoạt động bình thường', NOW()),
('BOOK-2026-005', 5, 4, 6, DATE_ADD(NOW(), INTERVAL 7 DAY), 'CONFIRMED', NULL, NOW()),
('BOOK-2026-006', 6, 6, 5, DATE_ADD(NOW(), INTERVAL 10 DAY), 'PENDING', NULL, NOW()),
('BOOK-2026-007', 7, 7, 7, DATE_ADD(NOW(), INTERVAL 4 DAY), 'CANCELLED', 'Khách hủy do bận', NOW());

-- ============================================================
-- 11. REFRESH TOKENS (Quản lý phiên đăng nhập)
-- ============================================================
-- Sample refresh tokens (in practice, these are generated during login)
INSERT INTO refresh_tokens (user_id, token, expires_at, created_at) VALUES
(1, UUID(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW()),
(3, UUID(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW()),
(5, UUID(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW());

-- ============================================================
-- SUMMARY
-- ============================================================
-- Roles: 4 entries
-- Users: 7 entries (1 admin, 1 manager, 2 sales, 3 technicians)
-- Brands: 7 entries
-- Products: 14 entries (7 machines, 7 filters)
-- Product Images: 9 entries
-- Customers: 7 entries
-- Orders: 7 entries (with various statuses)
-- Order Items: 15 entries
-- Services: 7 entries
-- Maintenance Bookings: 7 entries (with various statuses)
-- Refresh Tokens: 3 entries
-- ============================================================
