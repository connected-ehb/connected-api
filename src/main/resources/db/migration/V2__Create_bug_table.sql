CREATE TABLE bugs (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      description TEXT NOT NULL,
                      route VARCHAR(512),
                      app_version VARCHAR(64),
                      user_id BIGINT NOT NULL,
                      created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                      CONSTRAINT fk_bugs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_bugs_created_at ON bugs(created_at);
