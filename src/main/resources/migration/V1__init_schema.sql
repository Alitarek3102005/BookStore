CREATE TABLE users (
                       user_id UUID PRIMARY KEY,
                       username VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       address VARCHAR(255),
                       role VARCHAR(50) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE categories (
                            id UUID PRIMARY KEY,
                            name VARCHAR(255) NOT NULL UNIQUE,
                            description VARCHAR(1000)
);

CREATE TABLE books (
                       id UUID PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       author VARCHAR(255) NOT NULL,
                       price NUMERIC(19, 2) NOT NULL,
                       quantity INTEGER NOT NULL,
                       description VARCHAR(1000),
                       imgurl VARCHAR(255),
                       category_id UUID REFERENCES categories(id)
);

CREATE TABLE orders (
                        id UUID PRIMARY KEY,
                        user_id UUID NOT NULL REFERENCES users(user_id),
                        total_price DOUBLE PRECISION NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP NOT NULL,
                        status VARCHAR(50) NOT NULL
);

CREATE TABLE order_items (
                             id UUID PRIMARY KEY,
                             order_id UUID NOT NULL REFERENCES orders(id),
                             book_id UUID NOT NULL REFERENCES books(id),
                             quantity INTEGER NOT NULL,
                             unit_price NUMERIC(19, 2) NOT NULL
);