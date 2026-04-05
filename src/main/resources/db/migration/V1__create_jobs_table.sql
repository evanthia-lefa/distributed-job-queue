CREATE TABLE jobs
(
    id            BIGSERIAL PRIMARY KEY,
    type          VARCHAR(255) NOT NULL,
    payload       JSONB        NOT NULL,
    status        VARCHAR(50)  NOT NULL
        CHECK (status IN ('PENDING', 'RUNNING', 'SUCCEEDED', 'FAILED')),
    priority      INTEGER      NOT NULL DEFAULT 0,
    run_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    attempt_count INTEGER      NOT NULL DEFAULT 0,
    max_attempts  INTEGER      NOT NULL DEFAULT 3,
    last_error    TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    started_at    TIMESTAMPTZ,
    finished_at   TIMESTAMPTZ
);

CREATE INDEX idx_jobs_poll
    ON jobs (status, run_at, priority DESC, id);