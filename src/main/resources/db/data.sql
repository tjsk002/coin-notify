CREATE TABLE coins
(
    id           SERIAL PRIMARY KEY,
    market       VARCHAR(255),
    korean_name  VARCHAR(255),
    english_name VARCHAR(255),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMP DEFAULT NULL
);