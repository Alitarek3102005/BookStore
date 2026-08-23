-- ============================================================
-- V1: Initial schema
-- Mirrors the JPA entities exactly as validated by Hibernate's
-- ddl-auto=validate against the live database.
--
-- NOTE: on the current database this version is covered by the
-- Flyway baseline row (installed as type=BASELINE), so this file
-- will NOT be re-executed there - it only matters for anyone
-- spinning up a brand new, empty database from scratch.
-- ============================================================

CREATE TABLE users (
                       user_id  UUID PRIMARY KEY,
                       username VARCHAR(255) NOT NULL,
                       email    VARCHAR(255) NOT NULL,
                       address  VARCHAR(255),
                       role     VARCHAR(50)  NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       enabled  BOOLEAN      NOT NULL DEFAULT TRUE,
                       CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE categories (
                            id          UUID PRIMARY KEY,
                            name        VARCHAR(255)  NOT NULL,
                            description VARCHAR(1000),
                            CONSTRAINT uq_categories_name UNIQUE (name)
);

CREATE TABLE books (
                       id          UUID PRIMARY KEY,
                       title       VARCHAR(255)   NOT NULL,
                       author      VARCHAR(255)   NOT NULL,
                       price       NUMERIC(19, 2) NOT NULL,
                       quantity    INTEGER        NOT NULL,
                       description VARCHAR(1000),
                       imgurl      VARCHAR(255),
                       category_id UUID,
                       CONSTRAINT fk_books_category
                           FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE orders (
                        id          UUID PRIMARY KEY,
                        user_id     UUID                     NOT NULL,
                        total_price DOUBLE PRECISION         NOT NULL,
                        created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                        updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                        status      VARCHAR(50)              NOT NULL,
                        CONSTRAINT fk_orders_user
                            FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE order_items (
                             id         UUID PRIMARY KEY,
                             order_id   UUID           NOT NULL,
                             book_id    UUID           NOT NULL,
                             quantity   INTEGER        NOT NULL,
                             unit_price NUMERIC(19, 2) NOT NULL,
                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id) REFERENCES orders (id),
                             CONSTRAINT fk_order_items_book
                                 FOREIGN KEY (book_id) REFERENCES books (id)
);