-- V5: Project Events Audit Trail
-- Logs actions related to a project (user joins, leaves, applies, PO changes, etc.)

CREATE TABLE project_events
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    type       VARCHAR(100) NOT NULL,
    message    TEXT         NOT NULL,
    project_id BIGINT       NOT NULL,
    user_id    BIGINT NULL,
    timestamp  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_project_events_project
        FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,

    CONSTRAINT fk_project_events_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_project_events_project_timestamp
    ON project_events (project_id, timestamp DESC);
