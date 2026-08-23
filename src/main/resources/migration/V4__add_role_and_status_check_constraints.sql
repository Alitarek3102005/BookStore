ALTER TABLE users
    ADD CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'CUSTOMER'));

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status CHECK (status IN ('PENDING', 'SHIPPED', 'COMPLETED', 'CANCELED'));