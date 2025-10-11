CREATE TABLE remember_me_tokens (
    token VARCHAR(256) NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_remember_me_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
