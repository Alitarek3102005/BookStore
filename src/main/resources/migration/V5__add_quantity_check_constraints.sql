
ALTER TABLE books
    ADD CONSTRAINT chk_books_quantity_nonnegative CHECK (quantity >= 0);

ALTER TABLE order_items
    ADD CONSTRAINT chk_order_items_quantity_positive CHECK (quantity > 0);