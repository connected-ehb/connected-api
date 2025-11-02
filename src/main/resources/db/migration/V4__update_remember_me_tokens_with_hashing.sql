-- Migration to support BCrypt-hashed remember-me tokens
-- Changes:
-- 1. Add ID column as primary key (auto-increment)
-- 2. Rename 'token' column to 'token_hash' (stores BCrypt hash)
-- 3. Add index on user_id for efficient lookup
-- 4. Remove old tokens (they're in plain text and incompatible with new hashing)

-- Drop existing table and recreate with new schema
DROP TABLE IF EXISTS remember_me_tokens;

CREATE TABLE remember_me_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(60) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,

    -- Foreign key to users table
    CONSTRAINT fk_remember_me_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- Index for efficient lookup by user
    INDEX idx_user_id (user_id)
);

-- Note: Old tokens are deleted because they're stored in plain text.
-- Users will need to log in again to get a new hashed token.
