CREATE TABLE async_job (
                           id                BIGSERIAL PRIMARY KEY,

                           type              VARCHAR(100) NOT NULL,          -- np. "RECALC_CYCLE_SCORES", "DELETE_MEIN_VERSION"
                           status            VARCHAR(20)  NOT NULL,          -- "QUEUED", "RUNNING", "DONE", "FAILED"

                           progress_percent  INTEGER,                        -- 0–100
                           phase             VARCHAR(255),                   -- np. "Deleting journals", "Recalculating scores"
                           message           VARCHAR(255),                   -- krótki opis dla UI

                           request_payload   TEXT,                           -- JSON: np. {"cycleId": 123}
                           result_payload    TEXT,                           -- JSON: np. {"updated": 100, "unmatched": 20}

                           error_message     TEXT,

                           created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                           finished_at       TIMESTAMPTZ
);

CREATE INDEX idx_async_job_type ON async_job(type);
CREATE INDEX idx_async_job_status ON async_job(status);
CREATE INDEX idx_async_job_created_at ON async_job(created_at);
