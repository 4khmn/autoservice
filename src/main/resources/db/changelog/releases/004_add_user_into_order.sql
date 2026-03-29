-- liquibase formatted sql

-- changeset denis:4
ALTER TABLE orders
    ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1;

ALTER TABLE orders
    ADD CONSTRAINT fk_user
        FOREIGN KEY (user_id) REFERENCES users(id);