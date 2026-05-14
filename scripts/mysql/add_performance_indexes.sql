-- Run on MySQL/TiDB production database.
-- Note: if an index already exists with same columns/name, adjust manually.

ALTER TABLE orders
    ADD INDEX idx_orders_status_created_at (status, created_at),
    ADD INDEX idx_orders_sale_status_created_at (sale_id, status, created_at),
    ADD INDEX idx_orders_customer_created_at (customer_id, created_at);

ALTER TABLE payments
    ADD INDEX idx_payments_ref_type_ref_id_created (reference_type, reference_id, created_at);

ALTER TABLE maintenance_bookings
    ADD INDEX idx_bookings_status_booking_date (status, booking_date),
    ADD INDEX idx_bookings_technician_status_booking_date (technician_id, status, booking_date),
    ADD INDEX idx_bookings_customer_created_at (customer_id, created_at);

ALTER TABLE conversations
    ADD INDEX idx_conversations_customer_status (customer_id, status),
    ADD INDEX idx_conversations_status_last_message_at (status, last_message_at),
    ADD INDEX idx_conversations_admin_status_last_message_at (assigned_admin_id, status, last_message_at);

ALTER TABLE products
    ADD INDEX idx_products_deleted_type_brand (is_deleted, product_type, brand_id),
    ADD INDEX idx_products_deleted_stock (is_deleted, stock_quantity);

ALTER TABLE customers
    ADD INDEX idx_customers_created_by (created_by);
