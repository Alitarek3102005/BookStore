CREATE TABLE reviews (
                         id         UUID PRIMARY KEY,
                         book_id    UUID    NOT NULL,
                         user_id    UUID    NOT NULL,
                         rating     INTEGER NOT NULL,
                         comment    VARCHAR(1000),
                         created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                         updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                         CONSTRAINT fk_reviews_book FOREIGN KEY (book_id) REFERENCES books (id),
                         CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (user_id),
                         CONSTRAINT uq_reviews_book_user UNIQUE (book_id, user_id),
                         CONSTRAINT chk_reviews_rating_range CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_reviews_book_id ON reviews (book_id);
CREATE INDEX idx_reviews_user_id ON reviews (user_id);