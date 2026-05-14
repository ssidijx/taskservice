CREATE TABLE tasks (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    duration_ms  BIGINT       NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    result       TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at   TIMESTAMP,
    version      BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_tasks_status ON tasks (status);