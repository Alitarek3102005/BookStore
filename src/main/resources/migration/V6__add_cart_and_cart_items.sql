CREATE TABLE carts (
                       id         UUID PRIMARY KEY,
                       user_id    UUID NOT NULL,
                       created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                       updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                       CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users (user_id),
                       CONSTRAINT uq_carts_user UNIQUE (user_id)
);

CREATE TABLE cart_items (
                            id       UUID PRIMARY KEY,
                            cart_id  UUID    NOT NULL,
                            book_id  UUID    NOT NULL,
                            quantity INTEGER NOT NULL,
                            CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id),
                            CONSTRAINT fk_cart_items_book FOREIGN KEY (book_id) REFERENCES books (id),
                            CONSTRAINT chk_cart_items_quantity_positive CHECK (quantity > 0),
                            CONSTRAINT uq_cart_items_cart_book UNIQUE (cart_id, book_id)
);

CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX idx_cart_items_book_id ON cart_items (book_id);