ALTER TABLE bugs MODIFY COLUMN user_id BIGINT NULL;

ALTER TABLE bugs DROP FOREIGN KEY fk_bugs_user;

ALTER TABLE bugs
    ADD CONSTRAINT fk_bugs_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE SET NULL;
